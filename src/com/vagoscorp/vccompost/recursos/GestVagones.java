package com.vagoscorp.vccompost.recursos;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.SimpleBooleanProperty;

public class GestVagones {
    private Vagon[] vagones; //lista de vagones del equipo
    public Integer[] prioridades; //lista de prioridades para los vagones
    private Integer vagCreadosHoy=0;  //contador de vagones creados hoy
    public final Integer nVmax;     //numero maximo de vagones a almacenarse en la lista
    private Integer fecha=99;           //fecha del ultimo vagon agregado
    private String pathRel;         //path relativo 
    private String pathReg;
    public Integer vagExistentes=0;
    public SimpleBooleanProperty  disponible=new SimpleBooleanProperty(true);
    /**
     * Crea un nuevo gestor de vagones
     * @param maxVag Numero maximo de vagones
     * @param dir Path de la carpeta general de almacenamiento de datos
     */
    public GestVagones(int maxVag,String dir){
        this.nVmax=new Integer(maxVag);
        this.pathRel=dir;
        vagones=new Vagon[maxVag];
        pathReg=pathRel+"data//";
        //verificar existencia de directorio
        if(Files.notExists(Paths.get(pathReg))){
                try {
                        Files.createDirectories(Paths.get(pathReg));
                } catch (IOException e) {
                        System.out.println("No se pudo crear la carpeta verificar si ya existe");
                        //incorporar eventos de gestion de errores 
                }
        }
        //crear archivo de registro
        pathReg=pathReg+"vgrg.vcdt";
        try {
                Files.createFile(Paths.get(pathReg));
        } catch (IOException e) {
                System.out.println("No se pudo crear el archivo ver posibles soluciones");
        }
    }
    /**
     * Reconstruye el Registro de vagones y temperaturas en base al archivo de registro correspondiente
     * @param dir path de la carpeta donde se almacena el archivo de registro
     * @throws FileNotFoundException es lanzada en caso de no existir el archivo de registro en la carpeta especificada
     * @throws IOException es lanzada en caso de no poder leer el archivo de registro
     */
    
    public GestVagones(String dir) throws FileNotFoundException, IOException{
        int nm=0;
        this.pathRel=dir;
        pathReg=pathRel+"data//vgrg.vcdt";
        BufferedReader inputStream=null;
//        String res;
        int c=0;
        String l;
        inputStream=new BufferedReader(new FileReader(pathReg));
        while((l=inputStream.readLine())!=null){
            switch(c){
                case 0:{//numero maximo de vagones
                    nm=Integer.parseInt(l);
                    vagones=new Vagon[nm];
                    break;
                }
                case 1:{//fecha ultimo vagon agregado
                    fecha=Integer.parseInt(l);
                    break;
                }
                case 2:{//vagones creados hoy
                    vagCreadosHoy=Integer.parseInt(l);
                    break;
                }
                case 3:{//vagones existentes
                    vagExistentes=Integer.parseInt(l);
                    break;
                }
            }
            if((c>3)&&(c<(vagExistentes+4))){
                String[] dt=l.split(";"); //0-->path 1-->dia 2-->mes 3-->year 4-->numero de vagon  5-->posicion 6-->en muestreo
                String dp=dt[0];
                Integer dia=Integer.parseInt(dt[1]);
                Integer mes=Integer.parseInt(dt[2]);
                Integer year=Integer.parseInt(dt[3]);
                Integer nv=Integer.parseInt(dt[4]);
                Integer pos=Integer.parseInt(dt[5]);
                Boolean enS=Boolean.getBoolean(dt[6]);
                vagones[c-4]=new Vagon(dp, dia, mes, year, nv, pos, enS);
                
            }
            c++;
        }
        inputStream.close();
        this.nVmax=nm;
        

    }
    //metodos
    /**
     * Cambia el path de la carpeta general de almacenamiento no mueve los archivos ni cambia el comportamiento de los archivos creados previamente
     * @param dir path de la nueva carpeta
     */

    public void setPath(String dir){
        this.pathRel=dir;
    }
    /**
     * Devuelve el path de la carpeta de almacenamiento
     * @return 
     */
    public String getPath(){
        return pathRel;
    }
    
    
    /*
Formato de almacenamiento de vagones


nVmax
fecha ultimo vagon agregado
vagCreadosHoy
vagExistentes
path vagon1
path vagon2
.
.
.
path vagonN

posiblemente luego la lista de prioridades(no es seguro usarlas todavía)
*/
    /**
     * Almacena los datos cnecesarios para reconstruir los vagones en base a archivos
     */
    
    private void saveData(){
        PrintWriter oStReg=null;
        try {
            oStReg=new PrintWriter(new FileWriter(pathReg,false));  // se sobreescribira el archivo cada vez
            oStReg.println(this.nVmax.toString()); 
            oStReg.println(this.fecha.toString());
            oStReg.println(this.vagCreadosHoy.toString());
            oStReg.println(this.vagExistentes.toString());
            for(Vagon vg:vagones){
                if(vg!=null){
                    oStReg.println(vg.getRcData());
                }
            }
            oStReg.close();
        } catch (IOException ex) {
            Logger.getLogger(GestVagones.class.getName()).log(Level.SEVERE, null, ex);
            //agregar manejo de errores
        }
        
        //posiblemente la lista de prioridades tb
        
    }
    
    
    //metodos de gestion de vagones 
    /**
     * Agrega un nuevo vagon al registro 
     * @param desc Descripcion de las caracteristicas del vagon: materiales, observaciones, etc.
     */
    public void addVagon(String desc){
        Calendar tcr=Calendar.getInstance();
        Integer dia= tcr.get(Calendar.DAY_OF_MONTH);
        if(dia==fecha){
            fecha=dia;
            vagCreadosHoy=0;
            
        }
        vagCreadosHoy++;
        disponible.set(false);
        for(int i=(nVmax-1);i>0;i--){
            vagones[i]=vagones[i-1];
        }
        vagones[0]=new Vagon(0, 1, vagCreadosHoy, pathRel);
        int a=0;
        for(Vagon vg:vagones){
            a++;
            if(vg!=null){
                vg.num=a;
            }
        }
        for(int c=(nVmax-1);c>0;c--){
            if(vagones[c]!=null){
                vagones[c].renameTemp();
            }
        }

        if(vagExistentes<nVmax){
            vagExistentes++;
        }
        saveData();
        disponible.set(true);
    }
    /**
     * Inicializa el muestreo en el vagon especificado
     * @param v numero de vagon requerido de 1 a vagones existentes
     * @throws NullPointerException en caso de solicitar un vagon no existente todavía
     */
    public void initSampling(int v)throws NullPointerException{
        if((v<1)||(v>getNumVagones())){
            throw new NullPointerException("El indice requerido esta fuera del rango de vagones existentes: 0<v<=#devagones disponibles");
        }
        vagones[v-1].initSamples();
        saveData();
    }
    /**
     * Finaliza el muestreo en el vagon especificado
     * @param v numero de vagon requerido de 1 a vagones existentes
     * @throws NullPointerException en caso de solicitar un vagon no existente todavía
     */
    public void endSampling(int v)throws NullPointerException{
        if((v<1)||(v>getNumVagones())){
            throw new NullPointerException("El indice requerido esta fuera del rango de vagones existentes: 0<v<=#devagones disponibles");
        }
        vagones[v-1].endSamples();
        saveData();
    }
    /**
     * Agrega un par de jabalinas al vagon indicado
     * @param v  numero de vagon a modificarse de 1 a vagones existentes
     * @param jab lecturas a agregarse
     * @throws NullPointerException en caso de intentar modificar un vagon no existente
     * @throws IOException  en caso de no poder acceder a los archivos requeridos
     */
    public void addSample(int v,Jabalinas jab)throws NullPointerException, IOException{
        if((v<1)||(v>getNumVagones())){
            throw new NullPointerException("El indice requerido esta fuera del rango de vagones existentes: 0<v<=#devagones disponibles");
        }
        vagones[v-1].addSample(jab);

    }
    /**
     * Devuelve la informacion del vagon requerido como string con formato
     * @param v numero de vagon a modificarse de 1 a vagones existentes
     * @return Cadena con formato con la informacion requerida del vagon
     * @throws NullPointerException en caso de intentar modificar un vagon no existente
     * @throws IOException  en caso de no poder acceder a los archivos requeridos
     */
    public String getVag(int v)throws NullPointerException, IOException{
        if((v<1)||(v>nVmax)){
            throw new NullPointerException("El indice requerido esta fuera del rango de vagones existentes: 0<v<=#devagones");
        }
        if(vagones[v-1]!=null){
            return vagones[v-1].getVagon();
        }
        else{
            throw new NullPointerException("El vagon requerido no ha sido inicializado");
        }
    }
    
    public String getParJab(int v,int i)throws NullPointerException{
        if((v<1)||(v>nVmax)){
            throw new NullPointerException("El indice requerido esta fuera del rango de vagones existentes: 0<v<=#devagones");
        }
        if(vagones[v-1]!=null){
            return vagones[v-1].getParJabalinas(i);
        }
        else{
            throw new NullPointerException("El vagon requerido no ha sido inicializado");
        }
    }
    public String getJab(int v,int i,int j)throws NullPointerException{
        if((v<1)||(v>nVmax)){
            throw new NullPointerException("El indice requerido esta fuera del rango de vagones existentes: 0<v<=#devagones");
        }
        if(vagones[v-1]!=null){
            return vagones[v-1].getJabalina(i, j);
        }
        else{
            throw new NullPointerException("El vagon requerido no ha sido inicializado");
        }
    }
    /**
     * 
     * @return Cantidad de vagones existentes
     */
    public Integer getNumVagones(){
        int cv=0;
        for(Vagon vg: vagones){
            if(vg!=null){
                cv++;
            }
        }
        return cv;
    }
    /**
     * 
     * @return Devuelve arreglo con las horas del ultimo muestreo finalizado por vagon
     */
    public String[] getLastH(){
        int i=getNumVagones();
        String[] horas= new String[i];
        for(int c=0;c<i;i++){
            horas[c]=vagones[c].getLastH();
        }
        return horas;
    }
    /**
     * 
     * @return Devuelve arreglo con las horas de la ultima jabalina agregada por vagon
     */
    public String[] getActualH(){
        int i=getNumVagones();
        String[] horas= new String[i];
        for(int c=0;c<i;i++){
            horas[c]=vagones[c].getActualH();
        }
        return horas;
    }
}
