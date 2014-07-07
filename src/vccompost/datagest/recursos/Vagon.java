package vccompost.datagest.recursos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Vagon {
	public Integer pr;//Prioridad todavia a ver si es necesaria
	private final Path pathRegistro;//path del archivo de registro
	private Path pathTemporal;//path del archivo temporal
	public Boolean enSampling=false;//variable de control para ver si el muestreo esta habilitado
                
    public String hultimoMuestreo;//hora del ultimo muestreo finalizado
        
    Integer dia;
    Integer mes;
	Integer year;
        
    private final SimpleDateFormat date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
        
        
        
    PrintWriter oStTemp=null;
    PrintWriter oStReg=null;
	/**
	 * Sistema de gestion de informacion de temperatura
	 * @param dirRegistro path del archivo de registro del vagon, ej: C://coso//cosito//compost//vagon.vcvg
     * @param dirEnvio path del archivo de envio a tablet
	 */
	public Vagon(String dirRegistro,String dirEnvio){
            pathRegistro=Paths.get(dirRegistro);
            pathTemporal=Paths.get(dirEnvio);
            if(Files.notExists(pathRegistro)){
                try {
                    Files.createFile(pathRegistro);
                } catch (IOException ex) {
                    System.out.println("Fallo al crear archivo de registro :");
                    System.out.println(pathRegistro.toString());
                    Logger.getLogger(Vagon.class.getName()).log(Level.SEVERE, null, ex);
                    
                }
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
		enSampling=true;
	}
	/**
	 * Funcion para agregar muestras al presente universo de datos
	 * Almacenamiento de los diferentes datos de temperatura y su ubicacion 
	 * la hora esta dada por el momento en el que se crea almacene la muestra
	 * @param x1 Posicion en x(relativa al vagon) de la primera jabalina
	 * @param y1 Posicion en y(relativa al vagon) de la primera jabalina
	 * @param dx Distancia en x entre la primera y segunda jabalina
	 * @param dy Distancia en y entre la primera y segunda jabalina
	 * @param a1 Temperatura primer sensor primera jabalina
	 * @param a2 Temperatura segundo sensor primera jabalina
	 * @param a3 Temperatura tercer sensor primera jabalina
	 * @param a4 Temperatura cuarto sensor primera jabalina
	 * @param b1 Temperatura primer sensor segunda jabalina
	 * @param b2 Temperatura segundo sensor segunda jabalina
	 * @param b3 Temperatura tercer sensor segunda jabalina
	 * @param b4 Temperatura cuarto sensor segunda jabalina
	 */ 
	public void addSample(Integer x1,
			Integer y1,
			Integer x2,
			Integer y2,
			Integer a1,
			Integer a2,
			Integer a3,
			Integer a4,
			Integer b1,
			Integer b2,
			Integer b3,
			Integer b4)throws IOException{
		if(enSampling){
			String jabalinas=x1.toString()+";"+
			y1.toString()+";"+
			date.format(new GregorianCalendar().getTime()).toString()+";"+
			a1.toString()+";"+
			a2.toString()+";"+
			a3.toString()+";"+
			a4.toString()+"&"+
			x2.toString()+";"+
			y2.toString()+";"+
			date.format(new GregorianCalendar().getTime()).toString()+";"+
			b1.toString()+";"+
			b2.toString()+";"+
			b3.toString()+";"+
			b4.toString();
			
			
			oStReg=new PrintWriter(new FileWriter(pathRegistro.toString(),true));  // se crean los writers con append en true
	        oStTemp=new PrintWriter(new FileWriter(pathTemporal.toString(),true));
	        oStTemp.println(jabalinas);
	        oStReg.println(jabalinas); // ver la manera de separar por muestras para poder mostrar mejor los datos luego
	        oStReg.close();
	        oStTemp.close();
		}
		else{
			System.out.println("No se inicializo muestreo");
		}
	}
	/**
	 * 
	 * @return Datos correspondientes al vagon almacenados en el archivo temporal como una String con formato: #<numero de jabalinas>#<Muestras>/
	 * 		   
	 * @throws IOException En caso de no poder acceder al archivo
	 */
	public String getVagon() throws IOException{
		Integer njab=0;                    //numero de jabalinas registradas en el archivo
		BufferedReader inputStream=null;
		String res;

		inputStream=new BufferedReader(new FileReader(pathTemporal.toString()));
		String l;
		String jabBuff="";
		while((l=inputStream.readLine())!=null){
			jabBuff+=l+"&";
			njab++;
		}
		res= "#"+njab.toString()+"#"+jabBuff+"/";
		inputStream.close();

		return res;
	}

        /**
         * 
         * @return Devuelve la hora del ultimo universo de muestras concluido
         */
        public String getLastH(){
            return hultimoMuestreo;
        }
        /**
         * Elimina el archivo de registro para envio
         */
        public void deleteSendF(){
        	try {
				Files.deleteIfExists(pathTemporal);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        /**
         * Traslanda el contenido del archivo de envio a uno nuevo eliminando el antiguo
         * @param newPathSend
         */
        public void renameSendF(String newPathSend){
        	
        	try {
				Files.deleteIfExists(Paths.get(newPathSend));
				Files.createFile(Paths.get(newPathSend));
				
				PrintWriter oStTempNew;
	        	oStTempNew=new PrintWriter(new FileWriter(newPathSend,true));
	        	BufferedReader inputStream=null;
	    		inputStream=new BufferedReader(new FileReader(pathTemporal.toString()));
	    		String l;
	    		while((l=inputStream.readLine())!=null){
	    			oStTempNew.println(l);
	    		}
	    		inputStream.close();
	    		oStTempNew.close();
	    		Files.deleteIfExists(pathTemporal);
	    		pathTemporal=Paths.get(newPathSend);
	    		
	    		
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	

        }
}
