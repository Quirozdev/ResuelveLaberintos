import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;


/*
Nombre del programa: Laberinto

Como utilizarse en la linea de comando: hicimos este proyecto en una interfaz gráfica, por lo que no se van a recibir
argumentos como el archivo de entrada o salida, esos van a poder seleccionados con un menu (JFileChooser).
Probamos correr el programita con java Laberinto, pero creemos que no se puede porque en este archivo .java hay varias
clases además de la del Laberinto y parece que no detecta el main desde la linea de comandos, pero corriendolo desde el Intellij
o el Netbeans, si lo ejecuta.

Equipo LionKings:

Gonzalez Martínez Armando
Kniffin Ortiz Joan Andres
Quiroz Osuna Luis Daniel

Fecha: 03/12/2021

-El programa, al mandar ejecutar, utilice los argumentos de la línea de comandos para extraer el nombre del archivo de texto a leer (1 punto).
Como lo hicimos en un entorno gráfico, esta parte de extraer el nombre del archivo de texto a leer, lo vamos a obtener desde
un menu donde se puedan seleccionar los archivos que contienen a los laberintos.


-En caso de que exista el archivo a leer (2 puntos):
    -Lea el archivo de texto especificado en modo de “Lectura únicamente” (Read only) y guarde en un arreglo en memoria el arreglo. (La función para leer el archivo de laberinto, cargaLaberinto() les será proporcionada)
     En este caso solo leemos el archivo y no lo modificamos de ninguna manera.
    -En caso de que no exista el archivo a leer, el programa debe terminar de ejecutarse y mostrar un mensaje especificando que el archivo no existe (La función proporcionada imprime un error genérico y devuelve null cuando no encuentra un archivo).
     En la parte de cargaLaberinto() modificamos el catch para que muestre un mensaje cuando un archivo no exista, sin emabargo el programa no va a terminar
     de ejecutarse, le vamos a dar la oportunidad al usuario de poner un nombre de archivo válido en el menu de selección de archivos
     ó que lo seleccione directamente con un click.
 */

/*
Nuestro objetivo es convertir la matriz bidimensional de caracteres que representa al laberinto en un grafo con vértices y arcos.
Vamos a tener como base las clases Grafo, Vertice y Arco implementadas con listas ligadas que ya se habían hecho en clase.
Además de los métodos para generar los caminos.
Tendremos que ajustar un poco los metodos, por que ya no vamos a tener la clase Registro, de la que formabamos todo el grafo.
 */


class Arco {
    /*
    a esta clase se le modificó su nombre, por el de unas coordenadas y también se le agregaron los atributos
    de coordenadas en fila y columna de la matriz bidimensional del laberinto.
    Estas coordenadas en forma de entero van a ser muy utiles cuando ya estemos pintando en el laberinto con '#' los caminos
    ya que son las que vamos a usar para indicar en que posiciones vamos a pintar el recorrido en la matriz bidimensional.
    Con las puras coordenadas en String, se complicaría estar obteniendo solo las cantidades enteras porque van a ser
    de la forma: (1, 0), por eso es que mejor tambien almacenamos sus coordenadas de manera numerica en la clase Arco y Vértice
     */
    String coordenadas_destino;
    int peso;
    int coordenadaEnFila;
    int coordenadaEnColumna;

    public Arco(String coordenadas_destino, int peso) {
        this.coordenadas_destino = coordenadas_destino;
        this.peso = peso;
    }

}

class Vertice {
    //se modificó lo mismo que en la clase Arco
    String coordenadas;
    int coordenadaEnFila;
    int coordenadaEnColumna;
    boolean visto;
    //vamos a implementar nuestro grafo con LinkedLists, para cada Vertice se tiene una LinkedList de arcos asociadas a él.
    LinkedList<Arco> arcos;

    public Vertice(String nombre) {
        this.coordenadas = nombre;
        this.visto = false;
        this.arcos = new LinkedList<>();
    }

    public void display() {
        System.out.print(this.coordenadas + " >>|");
        displayArcos();
        //System.out.println("\n");
    }

    //este metodo fue un poquito modificado, ahora también recibe las coordenadas en forma númerica
    public void addEdge(Vertice destino, int coordenadasF, int coordenadasC, int peso) {
        Arco arco = new Arco(destino.coordenadas, peso);
        arco.coordenadaEnFila = coordenadasF;
        arco.coordenadaEnColumna = coordenadasC;
        this.arcos.add(arco);
    }

    public void displayArcos() {
        for (Arco arco : arcos) {
            System.out.print(arco.coordenadas_destino + ":" + arco.peso + " |");
        }
        System.out.println("");
    }
}

class Grafo {
    //grafo compuesto por una linkedlist de vertices
    LinkedList<Vertice> vertices;

    public Grafo() {
        this.vertices = new LinkedList<>();
    }

    public void limpiarVisto() {
        for (Vertice vertice : vertices) {
            vertice.visto = false;
        }
    }

    //este método también fue modificado, ahora también se reciben las coordenadas.
    public void addVertice(String nombre, int coordenadasF, int coordenadasC) {
        if (verticeExists(nombre) == false) {
            Vertice v = new Vertice(nombre);
            v.coordenadaEnFila = coordenadasF;
            v.coordenadaEnColumna = coordenadasC;
            this.vertices.add(v);
        }
    }

    //este método permanece igual a como lo hicimos en clase como algunos otros, solo que ahora se checan que las
    //coordenadas en forma String, no se encuentren ya en la lista ligada de vertices
    public boolean verticeExists(String nombre) {
        for (Vertice vertice : this.vertices) {
            if (vertice.coordenadas.equalsIgnoreCase(nombre)) {
                return true;
            }
        }
        return false;
    }

    //este método fue ajustado, porque como ya no se usa un objeto Registro y los archivos csv de donde se extraía esa información,
    //ahora los vamos a crear en base a la matriz bidimensional del laberinto
    public void crearVertices(char[][] laberinto) {
        //necesitamos ciclar sobre sus filas y columnas
        for (int i = 0; i < laberinto.length; i++) {
            for (int j = 0; j < laberinto[0].length; j++) {
                char temp = laberinto[i][j];
                //las paredes del laberinto van a ser los caracteres]: + - |
                //y nuestros vértices van a ser los espacios en blanco, por lo que son esos caracteres
                //los que se tienen que transformar a vértices
                if (temp == ' ') {
                    //se forma el nombre/coordenadas para el vértice
                    String coordenadas = "(" + i + ", " + j + ")";
                    //se pasa esta el nombre y coordenadas del vértice a este método para que lo cree y lo agregue
                    //a la lista ligada de vértices
                    addVertice(coordenadas, i, j);
                }
            }
        }
    }

    public void displayVertices() {
        for (Vertice vertice : this.vertices) {
            vertice.display();
        }
    }

    //al igual que el crearVertices(), este método se va a basar en el laberinto bidimensional de caracteres.
    public void crearArcos(char[][] laberinto) {
        //vamos a ciclar nuevamente sobre toda la matriz
        for (int i = 0; i < laberinto.length; i++) {
            for (int j = 0; j < laberinto[0].length; j++) {
                //si el elemento actual es un vertice y no una pared
                if (laberinto[i][j] == ' ') {
                    String coordenadas = "(" + i + ", " + j + ")";
                    //se busca a ese vertice origen (el actual)
                    Vertice origen = buscarVertice(coordenadas);
                    //se buscan todas sus adyacencias/vecinos y se agregan como sus arcos
                    buscarAdyacencia(laberinto, origen, i, j);
                }
            }
        }
    }

    //este método es para agregar como arcos aquellos vértices que estén "arriba, abajo, a la izquierda o a la derecha" de un vértice dado
    public void buscarAdyacencia(char[][] laberinto, Vertice origen, int i, int j) {
        //adyacencia hacia arriba
        //primero con este if checamos que no nos vayamos a salir del tamanio del arreglo como en la posición -1
        //para que no nos arroje un ArrayOutOfBoundsException
        if (i - 1 >= 0) {
            //si no se sale de la matriz bidimensional, entonces checamos si es un vértice, viendo si ese cáracter es un espacio en blanco.
            if (laberinto[i - 1][j] == ' ') {
                //formamos al arco
                String coordenadas = "(" + (i - 1) + ", " + j + ")";
                //para este punto ya hemos creado todos los vértices del grafo, por lo que este arco lo buscamos dentro
                //de los vertices que ya existen
                Vertice destino = buscarVertice(coordenadas);
                //al vértice dado (origen), le agregamos como arco ese vértice destino
                //se le pone como peso = 1, porque están "1 posición" aparte del vértice origen para alguno de sus lados.
                origen.addEdge(destino, i - 1, j, 1);
            }
        }
        //misma lógica para checar por los otros lados
        //adyacencia hacia abajo
        if (i + 1 < laberinto.length) {
            if (laberinto[i + 1][j] == ' ') {
                String coordenadas = "(" + (i + 1) + ", " + j + ")";
                Vertice destino = buscarVertice(coordenadas);
                origen.addEdge(destino, i + 1, j, 1);
            }
        }
        //adyacencia hacia la izquierda
        if (j - 1 >= 0) {
            if (laberinto[i][j - 1] == ' ') {
                String coordenadas = "(" + i + ", " + (j - 1) + ")";
                Vertice destino = buscarVertice(coordenadas);
                origen.addEdge(destino, i, j - 1, 1);
            }
        }
        //adyacencia hacia la derecha
        if (j + 1 < laberinto[0].length) {
            if (laberinto[i][j + 1] == ' ') {
                String coordenadas = "(" + i + ", " + (j + 1) + ")";
                Vertice destino = buscarVertice(coordenadas);
                origen.addEdge(destino, i, j + 1, 1);
            }
        }
    }

    //este método permanece igual a como lo teníamos en clase
    public Vertice buscarVertice(String coordenadas) {
        for (Vertice vertice : this.vertices) {
            if (vertice.coordenadas.equalsIgnoreCase(coordenadas)) {
                return vertice;
            }
        }
        return null;
    }


    //se va a usar una lista ligada porque
    //se va a hacer uso de métodos utiles como el .clear(), el .add() y además
    //de tener la posibilidad de recorrerla facilmente con un foreach, para usarla en métodos posteriores
    public LinkedList<Vertice> DepthFirstSearch(String inicio, String destino) {
        LinkedList<Vertice> camino = new LinkedList<>();
        limpiarVisto(); //Ponemos en false los campos "visto" de vertices
        boolean ENCONTRADO = false;
        Stack<Vertice> pila = new Stack<>();
        Vertice v = buscarVertice(inicio);
        pila.push(v);
        do {
            //despliegaStack(pila);
            v = pila.pop();
            //agregamos el vértice recorrido al camino que se va a formar
            camino.add(v);
            if (v.coordenadas.equals(destino)) {
                ENCONTRADO = true;
            } else {
                v.visto = true;
                agregarVecinos(pila, v);
            }
        } while (pila.isEmpty() == false && ENCONTRADO == false);
        System.out.println("DEPTH FIRST SEARCH");
        if (ENCONTRADO == true) {
            System.out.println("Existe un camino entre " + inicio + " y " + destino);
        } else {
            System.out.println("NO HAY un camino entre " + inicio + " y " + destino);
            //si no encontró un camino, entonces se vacía el camino que se intentó formar
            camino.clear();
        }
        return camino;
    }


    //se usa la misma lógica del camino del DFS aquí también
    public LinkedList<Vertice> BreadthFirstSearch(String inicio, String destino) {
        LinkedList<Vertice> camino = new LinkedList<>();
        limpiarVisto(); //Ponemos en false los campos "visto" de vertices
        boolean ENCONTRADO = false;
        Queue<Vertice> cola = new LinkedList<>();
        Vertice v = buscarVertice(inicio);
        cola.add(v);
        do {
            //despliegaQueue(cola);
            v = cola.remove();
            camino.add(v);
            if (v.coordenadas.equals(destino)) {
                ENCONTRADO = true;
            } else {
                v.visto = true;
                agregarVecinosQ(cola, v);
            }
            //despliegaStack(pila);
        } while (cola.isEmpty() == false && ENCONTRADO == false);
        System.out.println("BREADTH FIRST SEARCH");
        if (ENCONTRADO == true) {
            System.out.println("Existe un camino entre " + inicio + " y " + destino);
        } else {
            System.out.println("NO HAY un camino entre " + inicio + " y " + destino);
            //si no encontró un camino, se borra el intento de camino que se quiso formar
            camino.clear();
        }

        return camino;
    }

    //este metodo de agregarVecinosQ para el BFS que estaba por defecto, por alguna razón extrania hacía que el BFS
    //se volviera loco con algunos laberintos, en el camino se repetia un mismo vértice hasta 4 veces y se formaban
    //adyacencias entre un vértice y el mismo, causado presuntamente por este método, lo complicado de identificar este
    //problema es que el DFS funcionaba perfectamente con los laberintos en los que el BFS mostraba fallos, se tardaba demasiado
    //en resolver el laberinto y en el graphviz, el camino generado por el BFS era incorrecto, algo raro pasaba con la
    //Queue que agregaba a los vertices de manera repetida e incorrecta.
    //Es por eso que modificamos este método para que en el if también checara que la cola no contuviera a un vértice en ese
    //instante (!q.contains(vecino)), para que no se repitieran, hemos hecho pruebas y el BFS ya no se tarda lo que se tardaba y parece que forma
    //los caminos de manera correcta sin repetir vértices.
    public void agregarVecinosQ(Queue<Vertice> q, Vertice vertice) {
        for (Arco arco : vertice.arcos) {
            Vertice vecino = buscarVertice(arco.coordenadas_destino);
            if (vecino.visto == false && !q.contains(vecino)) {
                q.add(vecino);
            }
        }
    }

    //en este le agregamos la misma comprobación para la pila, pero creemos que no es necesaria porque el DFS seguía
    //funcionando de la misma forma, no como el BFS; creemos que la Queue tiene algo raro en su método .add()
    public void agregarVecinos(Stack<Vertice> s, Vertice vertice) {
        for (Arco arco : vertice.arcos) {
            Vertice vecino = buscarVertice(arco.coordenadas_destino);
            if (vecino.visto == false && !s.contains(vecino)) {
                s.push(vecino);
            }
        }
    }

    public static void despliegaStack(Stack<Vertice> stack) {
        if (stack.isEmpty() == false) {
            System.out.print("Stack:");
            for (Vertice vertice : stack) {
                System.out.print(" " + vertice.coordenadas);
            }
            System.out.println("");
        }
    }

    public static void despliegaQueue(Queue<Vertice> queue) {
        if (queue.isEmpty() == false) {
            System.out.print("Queue:");
            for (Vertice vertice : queue) {
                System.out.print(" " + vertice.coordenadas);
            }
            System.out.println("");
        }
    }

    //este método junto al de regresar() se van a utilizar para cuando se genere el grafo para Graphviz
    //son necesarios para que vértices que no sean adyacentes entre sí no formen caminos (estará más explicado en cada uno de éstos métodos)
    //se checa si 2 vertices dados, uno origen y uno destino son adyacentes en el grafo
    //este método va a servir cuando se estén generando los caminos
    public boolean checarAdyacencia(Vertice vert1, Vertice vert2) {
        //nos aseguramos que ninguno de los 2 sean nulos
        if (vert1 != null && vert2 != null) {
            //se recorren los adyacentes del vertice origen
            for (Arco arco : vert1.arcos) {
                //si se encuentra un arco de ese vertice igual al vertice destino, entonces hay adyacencia
                if (arco.coordenadas_destino.equals(vert2.coordenadas)) {
                    return true;
                }
            }
        }
        //si no se encontró una adyacencia.
        return false;
    }


    //esta función va a servir para cuando se estén generando los caminos y sea posible estar preparado
    //cuando 2 vertices origen y destino no sean adyacentes entre sí en el camino, se va
    //a "regresar" hasta un vertice del propio camino que se formó en el DFS y el BFS, tal que
    //sea adyacente al vertice destino
    //ejemplo:
    /*
    camino por defecto generado por el DFS:
    Gilberto | Carolina | Karla | Angel | Hector | Dulce | Ernesto |
    entre Dulce y Ernesto no hay una adyacencia, por lo que se tiene que regresar
    hasta Hector que es adyacente a Ernesto y entonces el verdadero origen sería
    Hector y no Dulce, el cual se regresaría en este método.

    el proposito de esta función es que no genere un camino con vertices que no fueran
    adyacentes entre sí
    */
    public Vertice regresar(LinkedList<Vertice> camino, Vertice destino) {
        //para cada vertice del camino formado por los algoritmos de busqueda
        for (Vertice verdaderoOrigen : camino) {
            //para los arcos de cada vertice
            for (Arco arco : verdaderoOrigen.arcos) {
                //si uno de los arcos es igual al destino y si el destino no es igual que
                //el "verdadero origen"
                if (arco.coordenadas_destino.equals(destino.coordenadas) && !destino.equals(verdaderoOrigen)) {
                    //se regresa a ese vértice de "verdadero origen"
                    return verdaderoOrigen;
                }
            }

        }
        //confíamos en que nunca va a regresar nulo por la propia estrtuctura de los caminos y los grafos
        return null;
    }

    public String generaGrafo() {
        //aquí se le tiene que modificar lo del "}\n" para que ya cuando esté guardando, tenga al grafo
        //y las instrucciones para "dibujar" los caminos
        //String cadena = "digraph G {\n";
        String cadena = "";
        for (Vertice vertice : vertices) {
            for (Arco arco : vertice.arcos) {
                //las coordenadas se necesitan poner entre comillas, porque si no, el Graphviz marca error
                //"(1, 0)" -> "(1, 1)" [label="1"]        Este si lo acepta
                //(1, 0) -> (1, 1) [label="1"]            Este no lo acepta
                cadena = cadena + " " + "\"(" + vertice.coordenadaEnFila + ", " + vertice.coordenadaEnColumna + ")\"";
                cadena = cadena + " -> ";
                cadena = cadena + " " + "\"(" + arco.coordenadaEnFila + ", " + arco.coordenadaEnColumna + ")\"";
                cadena = cadena + " [label= \"" + arco.peso + "\"];";
                cadena = cadena + "\n";
            }
        }

        //cadena = cadena + "}\n";
        return cadena;
    }


    //este metodo sirve para formar el camino del DFS y que sea visible en el Graphviz
    public String generaCaminoDFS(LinkedList<Vertice> camino) {
        //se le da el formato para que pueda usarse en Graphviz Online
        String instrucciones = "";
        //se va a usar el ciclo for clásico, porque se va a a ocupar obtener el vertice
        //de una posición i y el vertice que sigue (i + 1), esto por que así quedó
        //el camino, como solo se agregaban vertices pero sin sus arcos (nombre_destino y peso)
        //se necesita obtener el vertice origen y el vertice destino, se hace haciendo uso
        //del metodo .get(i) y se le pasa el indice i, entonces para obtener el destino,
        //como el camino tuvo que haber puesto los vértices en forma ordenada en la lista ligada del camino este debe ser (i + 1).

        //otra ventaja es que el camino generado va a ser un par, ejemplo:
        //Hector -> Dulce
        //Dulce -> German
        //nunca va a haber algo como:
        //German ->
        //porque debe de haber un vertice origen y un vertice destino
        for (int i = 0; i < camino.size(); i++) {
            Vertice origen = camino.get(i);
            //aquí se asegura que cuando quiera obtener el voltaje destino, no vaya a salirse del tamaño
            //de la lista ligada, si es que ya se llegó hasta el vertice destino, eso quiere decir que el vertice
            //origen anterior ya era el destino
            if (i + 1 < camino.size()) {
                Vertice destino = camino.get(i + 1);
                /*
                MUY IMPORTANTE checar que el vertice origen y destino sean adyacentes entre sí,
                //porque si no el camino se formaba con vertices que no eran adyacentes entre sí,
                //ejemplo en el archivo de los nombres:
                Gilberto -> Carolina [color="red"]
                Carolina -> Karla [color="red"]
                Karla -> Angel [color="red"]
                Angel -> Hector [color="red"]
                Angel -> Dulce [color="red"]
                Angel -> Ernesto [color="red"]
                se formaban adyacencias entre vertices que no lo eran, pero esto sucedía por la manera
                //en que se obtenían los caminos:
                Gilberto | Carolina | Karla | Angel | Hector | Dulce | Ernesto |
                por defecto se formaban adyacencias entre vertices que en el grafo no lo eran,
                //por ejemplo Dulce -> Ernesto, porque uno estaba seguido del otro
                //es por eso que aquí se va a checar eso con esta función checarAdyacencia()
                */
                boolean adyacencia = checarAdyacencia(origen, destino);
                //si hay adyacencia, entonces todo normal, los vertices obtenidos anteriormente se colocan
                //como estaban
                if (adyacencia) {
                    //para el camino del DFS se le pone como rojo para la propiedad "color"
                    instrucciones = instrucciones + " " + "\"(" + origen.coordenadaEnFila + ", " + origen.coordenadaEnColumna + ")\"" + " -> " + "\"(" + destino.coordenadaEnFila + ", " + destino.coordenadaEnColumna + ")\"" + " [color=\"red\"]\n";
                    //ahora, en dado caso de que no lo sean, la función regresar() es utilizada
                } else {
                    //si no son adyacentes, entonces el vertice origen no es el correcto y se que buscar a uno
                    //anterior que si lo era con esta función (es como un parche):
                    origen = regresar(camino, destino);
                    instrucciones = instrucciones + " " + "\"(" + origen.coordenadaEnFila + ", " + origen.coordenadaEnColumna + ")\"" + " -> " + "\"(" + destino.coordenadaEnFila + ", " + destino.coordenadaEnColumna + ")\"" + " [color=\"red\"]\n";
                }


            }
        }
        return instrucciones;
    }

    //este metodo sirve para formar el camino del BFS y que sea visible en el Graphviz
    //es lo mismo que el generaCaminoDFS, solo que el color se le pone en "blue" para
    //que se diferencien los caminos
    public String generaCaminoBFS(LinkedList<Vertice> camino) {
        String instrucciones = "";
        for (int i = 0; i < camino.size(); i++) {
            Vertice origen = camino.get(i);
            if (i + 1 < camino.size()) {
                Vertice destino = camino.get(i + 1);
                boolean adyacencia = checarAdyacencia(origen, destino);

                if (adyacencia) {
                    //para el camino del BFS se le pone en azul la propiedad "color"
                    instrucciones = instrucciones + " " + "\"(" + origen.coordenadaEnFila + ", " + origen.coordenadaEnColumna + ")\"" + " -> " + "\"(" + destino.coordenadaEnFila + ", " + destino.coordenadaEnColumna + ")\"" + " [color=\"blue\"]\n";
                } else {
                    origen = regresar(camino, destino);
                    instrucciones = instrucciones + " " + "\"(" + origen.coordenadaEnFila + ", " + origen.coordenadaEnColumna + ")\"" + " -> " + "\"(" + destino.coordenadaEnFila + ", " + destino.coordenadaEnColumna + ")\"" + " [color=\"blue\"]\n";
                }


            }
        }
        return instrucciones;
    }


    //este metodo genera instrucciones para pintar al vertice origen y destino,
    //cambia de color sus bordes y letras
    public String pintaVerticesOrigenDestino(String origen, String destino) {
        String instrucciones = " " + "\"" + origen + "\"" + " [color=\"purple\"]\n";
        instrucciones = instrucciones + " " + "\"" + origen + "\"" + " [fontcolor=\"purple\"]\n";
        instrucciones = instrucciones + " " + "\"" + destino + "\"" + " [color=\"green\"]\n";
        instrucciones = instrucciones + " " + "\"" + destino + "\"" + " [fontcolor=\"green\"]\n";
        return instrucciones;
    }

    //se modificó este metodo para que no solo recibiera la estructura del grafo, sino que
    //también los caminos y el "disenio" que eran los colores de los vertices
    //en contenido_grafo viene empaquetado la estructura del grafo y los caminos
    public void grabaGrafo(String archivo_salida, String contenido_grafo) {
        try {

            BufferedWriter bf = new BufferedWriter(new FileWriter(new File(archivo_salida), false));
            //aquí se guardan todas las instrucciones, primero ese encabezado
            bf.write("digraph G {");
            //salto de linea para que no quede pegado
            bf.newLine();
            //aquí van todas las instrucciones: adyacencia entre vertices y caminos "dibujados"
            bf.write(contenido_grafo);
            //final
            bf.write("}");
            //se cierra el flujo para que se guarde
            bf.flush();
            bf.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }


    //para ver que todo funcionara
    public void desplegarCamino(LinkedList<Vertice> camino) {
        for (Vertice vertice : camino) {
            System.out.print(vertice.coordenadas + " | ");
        }
    }

    //este método "pinta" en el laberinto los vertices que fueron recorridos por los caminos generados,
    //ya sea por el DFS o el BFS
    public void resolverLaberinto(char[][] laberinto, LinkedList<Vertice> camino) {
        //se recorre la lista ligada de vertices recorridos en el camino
        for (Vertice v : camino) {
            //esta era la utilida de las coordenadas en forma númerica, aquí simplemente se va indicando
            //las posiciones donde se necesita pintar un '#' en la matriz bidimensional del laberinto
            laberinto[v.coordenadaEnFila][v.coordenadaEnColumna] = '#';
        }
    }


}



//la clase principal va a heredar de JFrame para que en su constructor se maneje toda la parte gráfica.
public class Laberinto extends JFrame {
    //esta variable va a servir para los menus de seleccion y guardado de archivos, se obtiene el directorio actual en el que
    //está el proyecto.
    //esta barrita va a servir para cuando el laberinto sea demasiado largo y no quepa en el área de texto, se pueda desplazar
    //de izquierda a derecha y arriba hacia abajo
    private static JScrollPane barra;
    //estos botones son los que van a realizar los procesos de cargar un laberinto, resolverlo por DFS, por BFS, guardar
    //la estructura del grafo formado con el laberinto para Graphviz, guardar el laberinto en su forma textual y para salir.
    private static JButton botonCargarArchivo;
    private static JButton resolverPorDFS;
    private static JButton resolverPorBFS;
    private static JButton guardarArchivoGraphviz;
    private static JButton guardarLaberinto;
    private static JButton botonSalir;
    //esta área de texto va a determinar el estado en el que se encuentra el programa (si no se ha cargado un laberinto, si se
    // guardado un archivo, si se pudo resolver el laberinto...)
    private static JTextArea areaDeEstado;
    private static JLabel estadoActual;
    //esta área de texto va a ser donde se muestre el laberinto y sus soluciones
    private static JTextArea areaLaberinto;
    //estos jfilechooser van a ser los menus que se muestren para seleccionar un archivo para cargarlo y para guardar un archivo
    private static JFileChooser escogeArchivos;
    private static JFileChooser guardaArchivos;
    //estos checkbox van a estar disponibles para seleccionarse y van a determinar como se va a guardar el archivo de Graphviz
    private static JCheckBox opcionCaminoDFS;
    private static JCheckBox opcionCaminoBFS;
    //estas variables las ponemos como globales, para que si ya se formaron algunas de ellas, éstas no se tengan que volver
    //a generar con sus métodos correspondientes, de modo que se optimiza un poquito el programa
    private static Grafo grafo;
    private static LinkedList<Vertice> caminoDFS;
    private static LinkedList<Vertice> caminoBFS;
    private static String inicio;
    private static String fin;
    Queue<String> iniciofin;
    private static char[][] laberinto;


    //aquí es donde se va a manejar toda la interfaz gráfica
    public Laberinto() {
        //para que nos dé libertad al momento de colocar nuestros componentes con coordenadas
        setLayout(null);
        //para que no se pueda cambiar de tamanio
        setResizable(false);
        //se cambia el color de fondo del frame
        getContentPane().setBackground(Color.black);
        //se forma todo lo referente al botón salir
        botonSalir = new JButton("Salir");
        botonSalir.setBounds(950, 500, 200, 50);
        botonSalir.setBorder(new LineBorder(Color.red));
        botonSalir.addActionListener((ActionEvent e) -> {
            //simplemente deja de ejecutar el programa
            System.exit(0);
        });
        areaLaberinto = new JTextArea();
        areaLaberinto.setEditable(false);
        areaLaberinto.setBounds(300, 30, 600, 600);
        //le cambiamos la fuente que por defecto tiene, porque la que tenía hacía que el laberinto apareciera chueco y no
        //se podía distinguir bien al laberinto, ni los caminos.
        //también le cambiamos el tamanio para que no se viera tan pequenio.
        areaLaberinto.setFont(new Font("Monospaced", Font.PLAIN, 22));
        estadoActual = new JLabel("Estado actual: ");
        estadoActual.setBounds(950, 20, 200, 40);
        estadoActual.setFont(new Font("Monospaced", Font.PLAIN, 18));
        estadoActual.setForeground(Color.yellow);
        areaDeEstado = new JTextArea();
        areaDeEstado.setEditable(false);
        areaDeEstado.setBounds(950, 60, 230, 40);
        areaDeEstado.setFont(new Font("Monospaced", Font.PLAIN, 18));
        //al principio, este va a ser el "estado" y va a cambiar conforme se carguen los laberintos, se resuelva por un método
        //o se guarde un archivo
        areaDeEstado.setText("Laberinto no cargado");
        areaDeEstado.setBackground(Color.red);
        escogeArchivos = new JFileChooser();
        //aquí es donde usamos la variable de directorio actual, para decirle a los jmenufilechoosers que empiecen a buscar
        //los archivos desde ahí
        guardaArchivos = new JFileChooser();
        barra = new JScrollPane();
        botonCargarArchivo = new JButton("Cargar Laberinto");
        botonCargarArchivo.setBounds(50, 50, 200, 40);
        botonCargarArchivo.setBorder(new LineBorder(Color.red));
        //cuando el boton de cargar laberinto es presionado
        botonCargarArchivo.addActionListener((ActionEvent e) -> {
            areaDeEstado.setText("Cargando laberinto...");
            areaDeEstado.setBackground(Color.MAGENTA);
            //para que no se puedan elegir carpetas para leerlas
            escogeArchivos.setFileSelectionMode(JFileChooser.FILES_ONLY);
            //cuando se presiona el boton de aceptar o cancelar, capturamos ese valor entero
            int opcion = escogeArchivos.showOpenDialog(this);
            //si se presionó "Aceptar"
            if (opcion == JFileChooser.APPROVE_OPTION) {
                try {
                    //cargamos el laberinto con el archivo que haya sido seleccionado
                    System.out.println(escogeArchivos.getSelectedFile().toString());
                    laberinto = cargaLaberinto(escogeArchivos.getSelectedFile().toString());
                    System.out.println("pruebaaaa");
                    //formamos el grafo
                    grafo = new Grafo();
                    grafo.crearVertices(laberinto);
                    grafo.crearArcos(laberinto);
                    iniciofin = detectarInicioFin(laberinto);
                    /*
                    for (String a: iniciofin) {
                        System.out.println("a = " + a);
                    }

                     */
                    inicio = iniciofin.poll();
                    fin = iniciofin.poll();
                    //aquí es donde se pinta el laberinto en el área de texto
                    areaLaberinto.setText(cargarTextoLaberinto(laberinto));
                    //esto es para que la barra funcione correctamente
                    barra.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    barra.setBounds(300, 30, 600, 600);
                    barra.getViewport().add(areaLaberinto);
                    areaDeEstado.setText("Laberinto cargado");
                    areaDeEstado.setBackground(Color.cyan);
                    //imprimeLaberinto(laberinto);
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null, "Carga un laberinto válido", "Archivo no válido", JOptionPane.ERROR_MESSAGE);
                    areaDeEstado.setText("Laberinto no válido");
                    areaDeEstado.setBackground(Color.red);
                }
            } else if (opcion == JFileChooser.CANCEL_OPTION) {
                areaDeEstado.setText("Cargado cancelado");
                areaDeEstado.setBackground(Color.red);
            }

        });
        resolverPorDFS = new JButton("Resolver por DFS");
        resolverPorDFS.setBounds(50, 100, 200, 40);
        resolverPorDFS.setBorder(new LineBorder(Color.red));
        //cuando se presione el botón de resolver por DFS
        resolverPorDFS.addActionListener((ActionEvent e) -> {
            //con esto nos aseguramos de que el laberinto ya se encuentre cargado, si el area donde debe de estar el laberinto
            //está vacía, entonces eso quiere decir que el laberinto no se ha cargado.
            if (!areaLaberinto.getText().isEmpty()) {
                areaDeEstado.setText("Resolviendo laberinto...");
                areaDeEstado.setBackground(Color.MAGENTA);
                //char[][] laberinto = cargaLaberinto(escogeArchivos.getSelectedFile().toString());
                iniciofin = detectarInicioFin(laberinto);
                //para asegurarnos de que haya 2 salidas en el laberinto y por ende, que sea posible que tenga solución
                if (iniciofin.size() >= 2) {
                    inicio = iniciofin.poll();
                    fin = iniciofin.poll();
                    caminoDFS = grafo.DepthFirstSearch(inicio, fin);
                    //en los métodos de DepthFirstSearch y BreadthFirstSearch, si no se lograba encontrar un camino
                    //entre 2 vértices, lo que hacíamos era vacíar la lista ligada con el método .clear(), por lo que
                    //si para este punto la lista ligada del camino no está vacía, entonces quiere decir que si se encontró
                    //un camino entre ambos vértices.
                    if (!caminoDFS.isEmpty()) {
                        //realizamos una copia del laberinto "original" para no mutar al laberinto original y que éste quede
                        //inutilizable cuando se quiera resolver por el BFS, porque ya tendría vertices pintados con "#"
                        char[][] copia = clonarLaberinto(laberinto);
                        grafo.desplegarCamino(caminoDFS);
                        //System.out.println(grafo.generaCaminoDFS(caminoDFS));
                        grafo.resolverLaberinto(copia, caminoDFS);
                        //el área de texto se actualiza y pinta los "#" en los vértices pertenecientes al camino en el laberinto.
                        areaLaberinto.setText(cargarTextoLaberinto(copia));
                        areaDeEstado.setText("Laberinto resuelto por DFS");
                        areaDeEstado.setBackground(Color.ORANGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "El laberinto no tiene solución", "Solución no encontrada", JOptionPane.INFORMATION_MESSAGE);
                        areaDeEstado.setText("El laberinto no tiene solución");
                        areaDeEstado.setBackground(Color.red);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "El laberinto debe de tener por lo menos 2 salidas", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    areaDeEstado.setText("El laberinto no tiene 2 salidas");
                    areaDeEstado.setBackground(Color.red);
                }


            } else {
                JOptionPane.showMessageDialog(null, "Primero carga el laberinto", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }

        });

        //misma lógica que para el resolverPorDFS.
        //el BFS suele tardar mucho más cuando los laberintos son muy grandes.
        resolverPorBFS = new JButton("Resolver por BFS");
        resolverPorBFS.addActionListener((ActionEvent e) -> {
            if (!areaLaberinto.getText().isEmpty()) {
                areaDeEstado.setText("Resolviendo laberinto...");
                areaDeEstado.setBackground(Color.MAGENTA);
                //char[][] laberinto = cargaLaberinto(escogeArchivos.getSelectedFile().toString());
                iniciofin = detectarInicioFin(laberinto);
                if (iniciofin.size() >= 2) {
                    inicio = iniciofin.poll();
                    fin = iniciofin.poll();
                    caminoBFS = grafo.BreadthFirstSearch(inicio, fin);
                    if (!caminoBFS.isEmpty()) {
                        char[][] copia = clonarLaberinto(laberinto);
                        //System.out.println(grafo.generaCaminoBFS(caminoBFS));
                        grafo.desplegarCamino(caminoBFS);
                        grafo.resolverLaberinto(copia, caminoBFS);
                        areaLaberinto.setText(cargarTextoLaberinto(copia));
                        areaDeEstado.setText("Laberinto resuelto por BFS");
                        areaDeEstado.setBackground(Color.PINK);
                    } else {
                        JOptionPane.showMessageDialog(null, "El laberinto no tiene solución", "Solución no encontrada", JOptionPane.INFORMATION_MESSAGE);
                        areaDeEstado.setText("El laberinto no tiene solución");
                        areaDeEstado.setBackground(Color.red);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "El laberinto debe de tener por lo menos 2 salidas", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    areaDeEstado.setText("El laberinto no tiene 2 salidas");
                    areaDeEstado.setBackground(Color.red);
                }

            } else {
                JOptionPane.showMessageDialog(null, "Primero carga el laberinto", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        });
        resolverPorBFS.setBounds(50, 150, 200, 40);
        resolverPorBFS.setBorder(new LineBorder(Color.red));


        opcionCaminoDFS = new JCheckBox("Generar con camino DFS");
        opcionCaminoDFS.setBounds(50, 300, 220, 40);
        opcionCaminoDFS.setBackground(Color.black);
        opcionCaminoDFS.setForeground(Color.yellow);
        opcionCaminoDFS.setFont(new Font("Monospaced", Font.PLAIN, 14));

        opcionCaminoBFS = new JCheckBox("Generar con camino BFS");
        opcionCaminoBFS.setBounds(50, 330, 220, 40);
        opcionCaminoBFS.setBackground(Color.black);
        opcionCaminoBFS.setForeground(Color.yellow);
        opcionCaminoBFS.setFont(new Font("Monospaced", Font.PLAIN, 14));


        guardarArchivoGraphviz = new JButton("Guardar para Graphviz");
        guardarArchivoGraphviz.setBounds(50, 250, 200, 40);
        guardarArchivoGraphviz.setBorder(new LineBorder(Color.red));
        guardarArchivoGraphviz.addActionListener((ActionEvent e) -> {
            areaDeEstado.setText("Guardando archivo...");
            areaDeEstado.setBackground(Color.MAGENTA);
            String instruccionesEstructura = "";
            //checamos que el laberinto ya haya sido cargado y por ende el grafo ya haya sido creado, para poder formar las instrucciones.
            if (grafo != null) {
                instruccionesEstructura = grafo.generaGrafo();
                String instruccionesDFS = "";
                if (opcionCaminoDFS.isSelected()) {
                    //aquí checamos si el caminoDFS ya está formado para no volver a formarlo y de ese modo ahorrar memoria
                    if (caminoDFS != null) {
                        instruccionesDFS = grafo.generaCaminoDFS(caminoDFS);
                    } else {
                        caminoDFS = grafo.DepthFirstSearch(inicio, fin);
                        instruccionesDFS = grafo.generaCaminoDFS(caminoDFS);
                    }
                }
                String instruccionesBFS = "";
                if (opcionCaminoBFS.isSelected()) {
                    //aquí también checamos eso
                    if (caminoBFS != null) {
                        instruccionesBFS = grafo.generaCaminoBFS(caminoBFS);
                    } else {
                        caminoBFS = grafo.BreadthFirstSearch(inicio, fin);
                        instruccionesBFS = grafo.generaCaminoBFS(caminoBFS);
                    }
                }
                String instruccionesDisenio = "";
                if (inicio != null && fin != null) {
                    instruccionesDisenio = grafo.pintaVerticesOrigenDestino(inicio, fin);
                }

                int opcion = guardaArchivos.showSaveDialog(this);
                //si se eligió la opción de guardar
                if (opcion == JFileChooser.APPROVE_OPTION) {
                    String archivoSalida = guardaArchivos.getSelectedFile().toString();
                    //aquí checamos si el usuario ya puso .dot despues del archivo, si no, para incluirselo
                    if (archivoSalida.endsWith(".dot")) {
                        grafo.grabaGrafo(archivoSalida, instruccionesEstructura + instruccionesDFS + instruccionesBFS + instruccionesDisenio);
                    } else {
                        grafo.grabaGrafo(guardaArchivos.getSelectedFile().toString() + ".dot", instruccionesEstructura + instruccionesDFS + instruccionesBFS + instruccionesDisenio);
                    }
                    JOptionPane.showMessageDialog(null, "Archivo guardado con éxito");
                    areaDeEstado.setText("Archivo guardado");
                    areaDeEstado.setBackground(Color.green);
                } else if (opcion == JFileChooser.CANCEL_OPTION) {
                    areaDeEstado.setText("Guardado cancelado");
                    areaDeEstado.setBackground(Color.red);
                }


            } else {
                JOptionPane.showMessageDialog(null, "Carga el laberinto para formar el grafo", "Laberinto no cargado", JOptionPane.WARNING_MESSAGE);
                areaDeEstado.setText("Laberinto no cargado");
                areaDeEstado.setBackground(Color.red);
            }
        });

        //este botón guarda el laberinto que esté siendo mostrado en el área de texto donde se pinta,
        //si está resuelto por el DFS, BFS ó no resuelto aún, lo guarda tal cual esté en el área de texto
        //en ese momento
        guardarLaberinto = new JButton("Guardar laberinto resuelto");
        guardarLaberinto.setBounds(950, 430, 200, 50);
        guardarLaberinto.setBorder(new LineBorder(Color.red));
        guardarLaberinto.addActionListener((ActionEvent e) -> {
            areaDeEstado.setText("Guardando archivo...");
            areaDeEstado.setBackground(Color.MAGENTA);
            String instruccionesEstructura = "";
            //checamos que ya se haya cargado un laberinto
            if (!areaLaberinto.getText().isEmpty()) {
                //se obtiene todo lo que esté "pintado" en el área de texto donde esté el laberinto
                String laberintoTxt = areaLaberinto.getText();
                int opcion = guardaArchivos.showSaveDialog(this);
                if (opcion == JFileChooser.APPROVE_OPTION) {
                    String archivoSalida = guardaArchivos.getSelectedFile().toString();
                    if (!archivoSalida.endsWith(".txt")) {
                        archivoSalida = archivoSalida + ".txt";
                    }

                    File file = new File(archivoSalida);
                    try {
                        FileWriter fw = new FileWriter(file);
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(laberintoTxt);
                        bw.flush();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Error al tratar de guardar el archivo", "Error", JOptionPane.ERROR_MESSAGE);
                        //ex.printStackTrace();
                    }
                    JOptionPane.showMessageDialog(null, "Archivo guardado con éxito");
                    areaDeEstado.setText("Archivo guardado");
                    areaDeEstado.setBackground(Color.green);
                } else if (opcion == JFileChooser.CANCEL_OPTION) {
                    areaDeEstado.setText("Guardado cancelado");
                    areaDeEstado.setBackground(Color.red);
                }


            } else {
                JOptionPane.showMessageDialog(null, "Carga el laberinto", "Laberinto no cargado", JOptionPane.WARNING_MESSAGE);
                areaDeEstado.setText("Laberinto no cargado");
                areaDeEstado.setBackground(Color.red);
            }
        });

        //se agregan todos los componentes al JFrame
        add(botonSalir);
        add(estadoActual);
        add(areaDeEstado);
        add(barra);
        add(botonCargarArchivo);
        add(resolverPorDFS);
        add(resolverPorBFS);
        add(opcionCaminoDFS);
        add(opcionCaminoBFS);
        add(guardarLaberinto);
        add(guardarArchivoGraphviz);
        add(areaLaberinto);
        setTitle("Resuelve laberintos");
        //tamanio y coordenadas de la pantallita
        setBounds(120, 40, 1200, 800);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    //cuando se ejecuta el programa, se muestra la interfaz gráfica
    public static void main(String[] args) {
        Laberinto pantallita = new Laberinto();
        /*
        Scanner lector = new Scanner(System.in);
        while (true) {
            System.out.println("Selecciona una opción: 1 para Cargar archivo de laberinto, 2 para Resolver laberinto con DFS, 3 para Resolver laberinto con BFS, 0 para Salir  ");
            int opcion = lector.nextInt();
            lector.nextLine();
            char [][] laberinto = null;
            Grafo grafo = null;
            if (opcion == 1) {
                System.out.println("Escribe el nombre/ruta del archivo");
                String nombreArchivo = lector.nextLine();
                laberinto = cargaLaberinto(nombreArchivo);
                grafo = new Grafo();
                grafo.crearVertices(laberinto);
                grafo.crearArcos(laberinto);
                while (true) {
                    System.out.println("Presiona 2 para resolverlo por DFS, 3 para resolverlo por BFS, 0 para regresar");
                    opcion = lector.nextInt();
                    if (opcion == 0) {
                        break;
                    } else if (opcion == 2) {
                        if (grafo != null) {
                            Queue<String> inicioFin = detectarInicioFin(laberinto);
                            if (inicioFin.size() >= 2) {
                                String inicio = inicioFin.poll();
                                String destino = inicioFin.poll();
                                LinkedList<Vertice> caminoDFS = grafo.DepthFirstSearch(inicio, destino);
                                char[][] copia = clonarLaberinto(laberinto);
                                grafo.resolverLaberinto(copia, caminoDFS);
                                imprimeLaberinto(copia);
                            } else {
                                System.out.println("El laberinto debe de tener 2 salidas");
                            }

                        }

                    } else if (opcion == 3) {
                        if (grafo != null) {
                            Queue<String> inicioFin = detectarInicioFin(laberinto);
                            if (inicioFin.size() >= 2) {
                                String inicio = inicioFin.poll();
                                String destino = inicioFin.poll();
                                LinkedList<Vertice> caminoBFS = grafo.BreadthFirstSearch(inicio, destino);
                                char[][] copia = clonarLaberinto(laberinto);
                                grafo.resolverLaberinto(copia, caminoBFS);
                                imprimeLaberinto(copia);
                            } else {
                                System.out.println("El laberinto debe de tener 2 salidas");
                            }

                        }

                    }
                }
            }

        }

         */

    }


    public static char[][] cargaLaberinto(String archivo) {
        int i = 0;
        ArrayList<String> lineas = new ArrayList<>();
        try {
            File f = new File(archivo);
            BufferedReader b = new BufferedReader(new FileReader(f));

            String readLine = "";
            while ((readLine = b.readLine()) != null) {
                //System.out.println(readLine);
                lineas.add(readLine);
            }
            int ancho = lineas.get(0).length();
            int largo = lineas.size();
            char[][] mapa = new char[largo][ancho];
            for (String linea : lineas) {
                //System.out.println(linea);
                char[] caracteres = linea.toCharArray();
                for (int j = 0; j < caracteres.length; j++) {
                    mapa[i][j] = caracteres[j];
                    //System.out.print(mapa[i][j]);
                }
                //System.out.println();
                i++;
            }
            return mapa;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "No se encontró un archivo con ese nombre");
            //e.printStackTrace();
            return null;
        }

    }

    //para checar que cargara bien el laberinto, también es útil para imprimir el laberinto ya resuelto
    public static void imprimeLaberinto(char[][] laberinto) {
        for (int i = 0; i < laberinto.length; i++) {
            for (int j = 0; j < laberinto.length; j++) {
                System.out.print(laberinto[i][j]);
            }
            System.out.println();
        }
    }

    //este fue utilizado en un comienzo y era para ver como se comportaba, este método termino transformandose
    //en el método crearVertices() de la clase Grafo (con algunas modificaciones).
    public static void detectarVertices(char[][] laberinto) {
        System.out.println("Coordenadas de los vértices");
        for (int i = 0; i < laberinto.length; i++) {
            for (int j = 0; j < laberinto.length; j++) {
                char temp = laberinto[i][j];
                if (temp == ' ') {
                    System.out.println("(" + i + ", " + j + ")");
                }

            }
        }
    }

    //con este método se detectan las salidas de laberinto en sus bordes.
    //se regresa una Cola con las 2 salidas, que van a ser los vertices origen y destino para los
    //algoritmos DFS y BFS
    public static Queue<String> detectarInicioFin(char[][] laberinto) {
        Queue<String> iniciofin = new LinkedList<>();
        //System.out.println("Coordenadas de los vértices");
        //vamos a requerir estar checando los 4 bordes del laberinto para ver en donde están ubicadas las salidas.
        //para el borde superior horizontal
        for (int i = 0; i < laberinto[0].length; i++) {
            //aquí se va a estar recorriendo la primera fila y avanzando en las columnas.
            //si encontramos un vértice en el borde, entonces lo agregamos a la cola de salidas.
            char temp = laberinto[0][i];
            if (temp == ' ') {
                //se forman las coordenadas con el mismo formato que les dimos al crear los vértices y arcos.
                //esto para que luego las podamos buscar con el método buscarVertice()
                String coordenadas = "(" + 0 + ", " + i + ")";
                iniciofin.add(coordenadas);
            }
        }
        //para el borde izquierdo vertical
        for (int i = 0; i < laberinto.length; i++) {
            //aquí se va a estar recorriendo la primera columna y avanzando en las filas.
            //si encontramos un vértice en el borde, entonces lo agregamos a la cola de salidas.
            char temp = laberinto[i][0];
            if (temp == ' ') {
                String coordenadas = "(" + i + ", " + 0 + ")";
                iniciofin.add(coordenadas);
            }
        }

        //para el borde inferior horizontal
        for (int i = 0; i < laberinto[0].length; i++) {
            //aquí se va a estar recorriendo la ultima fila y avanzando en las columnas.
            //si encontramos un vértice en el borde, entonces lo agregamos a la cola de salidas.
            //laberinto.length - 1 porque los arreglos empiezan en la posición 0
            char temp = laberinto[laberinto.length - 1][i];
            if (temp == ' ') {
                String coordenadas = "(" + (laberinto.length - 1) + ", " + i + ")";
                iniciofin.add(coordenadas);
            }
        }

        //para el borde derecho vertical
        for (int i = 0; i < laberinto.length; i++) {
            //aquí se va a estar recorriendo la ultima columna y avanzando en las filas.
            //si encontramos un vértice en el borde, entonces lo agregamos a la cola de salidas.
            char temp = laberinto[i][laberinto[0].length - 1];
            if (temp == ' ') {
                String coordenadas = "(" + i + ", " + (laberinto[0].length - 1) + ")";
                iniciofin.add(coordenadas);
            }
        }

        return iniciofin;
    }


    //este método lo vamos a usar para copiar la matriz bidimensional del laberinto, porque no vamos a pintar los caminos
    //en la matriz del laberinto original, porque se "muta" y cuando vayamos a recorrerla con otro algoritmo, ya se va a
    //encontrar pintada con "#" en un principio, es por eso que creamos una nueva matriz bidimensional y le asignamos
    //los mismos valores que la matriz original
    public static char[][] clonarLaberinto(char[][] laberinto) {
        char[][] laberintoClon = new char[laberinto.length][laberinto[0].length];
        for (int i = 0; i < laberinto.length; i++) {
            for (int j = 0; j < laberinto[0].length; j++) {
                laberintoClon[i][j] = laberinto[i][j];
            }
        }
        return laberintoClon;
    }


    //este método es como el imprimeVertices(), pero va a ser utilizado para rellenar el JTextArea con los caracteres del laberinto
    public static String cargarTextoLaberinto(char[][] laberinto) {
        String laberintoTexto = "";
        for (int i = 0; i < laberinto.length; i++) {
            for (int j = 0; j < laberinto[0].length; j++) {
                laberintoTexto = laberintoTexto + laberinto[i][j];
            }
            laberintoTexto = laberintoTexto + "\n";
        }

        return laberintoTexto;
    }


}
