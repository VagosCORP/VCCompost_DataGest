/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vccompost.datagest.recursos;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Francisco
 */



public class GestorVagones {
   
	
	private final Integer nVmax;//numero maximo de vagones a almacenarse
	private String pathRel;	//direccion de la carpeta de almacenamiento de datos
	private String pathSend;//direccion de la carpeta de archivos de envio
	private Vagon[] vagones;//vagones del equipo 
	private String pathReg;//path de los archivos de registro permanentes del equipo
	
	
	private Integer fecha=99;  //fecha del ultimo vagon agregado
    private Integer vagCreadosHoy=0; //numero de vagones creados hoy
    private Integer vagExistentes=0; //numero de vagones existentes
    
    public SimpleBooleanProperty  disponible=new SimpleBooleanProperty(true);//control de flujo
	
    /**
     * Crea un nuevo gestor de vagones
     * @param maxVag Numero maximo de vagones
     * @param dir Path de la carpeta general de almacenamiento de datos
     */
    public GestorVagones(int maxVag,String dir){
    	this.nVmax=new Integer(maxVag);
        this.pathRel=dir;
        vagones=new Vagon[maxVag];
        pathReg=pathRel+"data//";
        pathSend=pathRel+"send//";
        //verificar existencia de directorios
        if(Files.notExists(Paths.get(pathReg))){
                try {
                        Files.createDirectories(Paths.get(pathReg));
                } catch (IOException e) {
                        System.out.println("No se pudo crear la carpeta de registros verificar si ya existe");
                        //incorporar eventos de gestion de errores 
                }
        }
        
        if(Files.notExists(Paths.get(pathSend))){
            try {
                    Files.createDirectories(Paths.get(pathSend));
            } catch (IOException e) {
                    System.out.println("No se pudo crear la carpeta de envio verificar si ya existe");
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
    
    public GestorVagones(String dir) throws FileNotFoundException, IOException{
    	int nm=0;
        this.pathRel=dir;
        pathReg=pathRel+"data//vgrg.vcdt";
        BufferedReader inputStream=null;
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
                String[] dt=l.split(";"); //0-->path Registro 1-->path Envio 2-->en muestreo
                String dp=dt[0];
                String pEnvio=dt[1];
                Boolean enS=Boolean.parseBoolean(dt[2]);
                vagones[c-4]=new Vagon(dp,pEnvio);
                vagones[c-4].enSampling=enS;
                vagones[c-4].hultimoMuestreo=dt[3];
                System.out.println("se recupero Vagon: "+vagones[c-4].getRegPath());
                System.out.println("archivo de envio: "+vagones[c-4].getTempPath());
                System.out.println("Muestreo Habilitado: "+vagones[c-4].enSampling);
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
        pathRel=dir;
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

posiblemente luego la lista de prioridades(no es seguro usarlas todavÃ­a)
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
                    oStReg.println(vg.getRegPath()+";"+vg.getTempPath()+";"+vg.enSampling.toString()+";"+vg.getLastH()+";");
                }
            }
            oStReg.close();
        } catch (IOException ex) {
            Logger.getLogger(GestorVagones.class.getName()).log(Level.SEVERE, null, ex);
            //agregar manejo de errores
        }
                
    }
    
    
    //metodos de gestion de vagones 
    /**
     * Agrega un nuevo vagon al registro 
     * @param desc Descripcion de las caracteristicas del vagon: materiales, observaciones, etc.
     */
    public void addVagon(String desc){
    	Calendar tcr=Calendar.getInstance();
        Integer dia= tcr.get(Calendar.DAY_OF_MONTH);
        Integer mes= tcr.get(Calendar.MONTH)+1;
        Integer year=tcr.get(Calendar.YEAR);
        
        if(dia!=fecha){
            fecha=dia;
            vagCreadosHoy=0;
            
        }
        vagCreadosHoy++;
        disponible.set(false);
        for(int i=(nVmax-1);i>0;i--){
            vagones[i]=vagones[i-1];
            if(vagones[i]!=null){
                vagones[i].renameSendF(pathSend+(i+1)+".vctemp");
            }
        }
        
        Path dirRegVag=Paths.get(pathRel+"data//"+year+"//"+mes+"//");
        
        
        
        if(Files.notExists(dirRegVag)){
            try {
                    Files.createDirectories(dirRegVag);
            } catch (IOException e) {
                    System.out.println("No se pudo crear la carpeta de envio verificar si ya existe");
                    //incorporar eventos de gestion de errores 
            }
        }
        
//        <día>-vg<#de vagoncreado>.vcdt
//        Archivo de envio:
//        <posicion 1-32>.vctemp
        vagones[0]=new Vagon(dirRegVag.toString()+"//"+dia.toString()+"-vg"+vagCreadosHoy.toString()+".vcdt",
        		pathSend+"1"+".vctemp");

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
     * @throws NullPointerException en caso de solicitar un vagon no existente todavÃ­a
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
    public void addSample(int v,int x1,int y1,int x2,int y2,Integer a1,Integer a2,Integer a3,Integer a4,Integer b1,Integer b2,Integer b3,Integer b4)throws NullPointerException, IOException{
    	if((v<1)||(v>getNumVagones())){
            throw new NullPointerException("El indice requerido esta fuera del rango de vagones existentes: 0<v<=#devagones disponibles");
        }
        vagones[v-1].addSample(x1, y1, x2, y2, a1, a2, a3, a4, b1, b2, b3, b4);
    }
    /**
     * Devuelve la informacion del vagon requerido como string con formato
     * @param v numero de vagon a modificarse de 1 a vagones existentes
     * @return Cadena con formato con la informacion requerida del vagon
     * @throws NullPointerException en caso de intentar modificar un vagon no existente
     * @throws IOException  en caso de no poder acceder a los archivos requeridos
     */
    public String getVag(int v)throws NullPointerException, IOException{
    	if((v<1)||(v>getNumVagones())){
            throw new NullPointerException("El indice requerido esta fuera del rango de vagones existentes: 0<v<=#devagones disponibles");
        }
    	return "1#"+v+vagones[v-1].getVagon();
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
        for(int c=0;c<i;c++){
            horas[c]=vagones[c].getLastH();
        }
        return horas;
    }

}
