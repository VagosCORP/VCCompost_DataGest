package com.vagoscorp.vccompost.recursos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Vagon {
	
	public Integer num;//posicion del vagon en el proceso 
    public final Integer nVgCreado;//numero de vagon creado en el dia
	public Integer pr;//Prioridad todavia a ver si es necesaria
	private final List<Jabalinas> muestras= new ArrayList<>();//lista para almacenar las muestras de temperatura
	private final Path pathRegistro;//path del archivo de registro
	private Path pathTemporal;//path del archivo temporal
	private final String nombre;//nombre del vagon 
	private Boolean enSampling=false;//variable de control para ver si el muestreo esta habilitado
    private final String pathparcial;//path de la carpeta general de almacenamiento de datos     
    private String hultimaMuestra;// hora de la ultima muestra añadida
    private String hultimoMuestreo;//hora del ultimo muestreo finalizado   
    Integer dia;
    Integer mes;
	Integer year;   
    private final SimpleDateFormat date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
    PrintWriter oStTemp=null;
    PrintWriter oStReg=null;
    
	/**
	 * Sistema de gestion de informacion de temperatura
	 * @param posicion del vagon en el equipo (1-32)
	 * @param prioridad relevancia del vagon usada para la toma de decisiones del equipo
	 * @param nv numero de vagon creado en el dia
	 * @param dir path de la carpeta donde se tienen las carpetas de manejo de informaci�n del equipo, ej: C://coso//cosito//compost//
	 */
	public Vagon(Integer posicion,Integer prioridad,Integer nv,String dir){
            pathparcial=dir;
            Calendar tcr=Calendar.getInstance();
		dia= new Integer(tcr.get(Calendar.DAY_OF_MONTH));
		mes= new Integer(tcr.get(Calendar.MONTH)+1);
		year= new Integer(tcr.get(Calendar.YEAR));
		num=posicion;
                pr=prioridad;
		nVgCreado=nv;
                
                this.nombre=dia.toString()+"-vg"+nVgCreado.toString()+".vcdt";
		pathRegistro=Paths.get(dir+"data//"+year+"//"+mes+"//"+nombre);
		
		Path directorio=Paths.get(dir+"data//"+year+"//"+mes+"//");
                Path dirtemp=Paths.get(dir+"data//temp//");
		pathTemporal=Paths.get(dir+"data//temp//"+num.toString()+".vctemp");
		
		
		//verificar existencia de directorio
		if(Files.notExists(directorio)){
			try {
				Files.createDirectories(directorio);
			} catch (IOException e) {
				System.out.println("No se pudo crear la carpeta verificar si ya existe");
			}
		}
                //verificar la existencia del directorio temporal
                if(Files.notExists(dirtemp)){
			try {
				Files.createDirectories(dirtemp);
			} catch (IOException e) {
				System.out.println("No se pudo crear la carpeta de archivos temporales verificar si ya existe");
			}
		}
		//crear archivo de registro
		try {
			Files.createFile(pathRegistro);
		} catch (IOException e) {
			System.out.println("No se pudo crear el archivo de registro ver posibles soluciones");
		}

	}
        /**
         * Asocia un vagon registrado con uno nuevo para poder seguir agregando información
         * @param dir path de la carpeta de registros
         * @param dia de creacion del vagon
         * @param mes de creacion del vagon
         * @param a�o de creacion del vagon
         * @param nv  numero de vagon creado ese dia
         * @param pos posicion del vagon en la cola de vagones
         * @param en  variable de control sobre si el muestreo estaba habilitado en ese momento
         */
        public Vagon(String dir,Integer dia,Integer mes,Integer year,Integer nv,Integer pos,Boolean en) throws IOException{
            nVgCreado=nv;
            this.nombre=dia+"-vg"+nv.toString()+".vcdt";
            pathRegistro=Paths.get(dir+"data//"+year+"//"+mes+"//"+nombre);
            pathparcial=dir;
            num=pos;
            
            pathTemporal=Paths.get(pathparcial+"data//temp//"+num.toString()+".vctemp");
            if(Files.notExists(pathTemporal)){
			throw new IOException("No existe el archivo temporal con el path :"+pathTemporal.toString()+"\n");
            }
            reconst();
            enSampling=en;
            if(Files.notExists(pathRegistro)){
			throw new IOException("No existe el archivo de registro con el path :"+pathRegistro.toString()+"\n");
            }
        }
	/**
	 * 
	 * @return String con el Path del archivo de registro
	 */
	public String getRegPath(){
		return pathRegistro.toString();
	}
	/**
	 * 
	 * @return String con el Path del archivo temporal
	 */
	public String getTempPath(){
		return pathTemporal.toString();
	}
	/**
	 * Finaliza el universo de muestras presente
	 */
	public void endSamples(){
            enSampling=false;
            hultimoMuestreo=date.format(new GregorianCalendar().getTime()).toString();
	}
	/**
	 * Inicializa un nuevo universo de muestras preparando los archivos necesarios
	 */
	public void initSamples(){
            
            pathTemporal=Paths.get(pathparcial+"data//temp//"+num.toString()+".vctemp");
            try {
			Files.deleteIfExists(pathTemporal);
		} catch (IOException e) {
			System.out.println("Problema al eliminar el archivo temporal buscar solucion");
			
		}
		try {
			Files.createFile(pathTemporal);
                        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Problema al crear el archivo temporal buscar solucion");
		}
		muestras.clear();
		enSampling=true;
		
	}
	/**
	 * Funcion para agregar muestras al presente universo de datos
	 * @param jab jabalina con la informacion adquirida a almacenarse
	 * @throws IOException En caso de no haberse inicializado el muestreo
	 */ 
	public void addSample(Jabalinas jab)throws IOException{
            
            if(enSampling){
			muestras.add(jab);
                        hultimaMuestra=date.format(new GregorianCalendar().getTime()).toString();
                        oStReg=new PrintWriter(new FileWriter(pathRegistro.toString(),true));  // se crean los writers con append en true
                        oStTemp=new PrintWriter(new FileWriter(pathTemporal.toString(),true));
                        oStTemp.println(jab.getBoth());
			oStReg.println(jab.getBoth()); // ver la manera de separar por muestras para poder mostrar mejor los datos luego
                        oStReg.close();
                        oStTemp.close();
		}
		else{
			throw new IOException("Se debe inicializar el muestreo");
		}
	}
	/**
	 * 
	 * @return Datos correspondientes al vagon almacenados en el archivo temporal como una String con formato
	 * @throws IOException En caso de no poder acceder al archivo
	 */
	public String getVagon() throws IOException{
		Integer njab=new Integer(muestras.size()*2);
		BufferedReader inputStream=null;
		String res;

		inputStream=new BufferedReader(new FileReader(pathTemporal.toString()));
		String l;
		String jabBuff="";
		while((l=inputStream.readLine())!=null){
			jabBuff+=l+"&";
		}
		res= "1#"+num.toString()+"#"+njab.toString()+"#"+jabBuff+"/";
		inputStream.close();

		return res;
	}
	/**
	 * 
	 * @param i indice de par de jabalinas almacenado 
	 * @return	par de jabalinas requerido en forma de String con formato
	 */
	public String getParJabalinas(int i){
		return muestras.get(i).getBoth();
	}
	/**
	 * 
	 * @param nM numero de muestra
	 * @param j	numero de jabalina de la muestra 1 o 2
	 * @return	datos de la jabalina como String con formato
	 * @throws NullPointerException en caso de pedirse una jabalina fuera del rango
	 */
	public String getJabalina(int nM,int j) throws NullPointerException{
		String jabl;
		if(j==1){
			jabl=muestras.get(nM).getJab1();
		}
		else if(j==2){
			jabl=muestras.get(nM).getJab2();
		}
		else{
			throw new NullPointerException("solo existen 2 jabalinas");
		}
		return jabl;
	}
        /**
         * Devuelve la información necesaria para la reconstrucción del vagon en base al registro almacenado
         * @return  cadena con el formato <path parcial>;<dia>;<mes>;<year>;<numero de vagon creado>;<pos>;<>
         */
        public String getRcData(){
            return pathparcial+";"
                    +dia.toString()+";"
                    +mes.toString()+";"+year.toString()+";"
                    +nVgCreado.toString()+";"
                    +num.toString()+";"
                    +enSampling.toString();
        }
        
        
        /**
         * 
         * @return Devuelve la hora del ultimo universo de muestras concluido
         */
        public String getLastH(){
            return hultimoMuestreo;
        }
        /**
         * 
         * @return Devuelve la hora de la ultima jabalina agregada
         */
        public String getActualH(){
            return hultimaMuestra;
        }
        public void deleteTemp(){
            try {
                    Files.deleteIfExists(pathTemporal);
            } catch (IOException e) {
                    System.out.println("Problema al eliminar el archivo temporal buscar solucion");

            }
        }
        public void renameTemp(){
            File antiguo= new File(pathTemporal.toString());
            pathTemporal=Paths.get(pathparcial+"data//temp//"+num.toString()+".vctemp");
            File nuevo= new File(pathTemporal.toString());
            antiguo.renameTo(nuevo);
        }
        private void reconst(){
            muestras.clear();
            try {
                BufferedReader inputStream=null;
                try {
                    inputStream=new BufferedReader(new FileReader(pathTemporal.toString()));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Vagon.class.getName()).log(Level.SEVERE, null, ex);
                }
                String l;
//                String jabBuff="";
                while((l=inputStream.readLine())!=null){
                    muestras.add(new Jabalinas(l));
                }
            } catch (IOException ex) {
                Logger.getLogger(Vagon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
}
