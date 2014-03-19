package com.vagoscorp.vccompost.recursos;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class Jabalinas {
	private final SimpleFloatProperty T1[]; // sensores primera javalina
	private final SimpleFloatProperty T2[]; // sensores segunda javalina

	private final SimpleIntegerProperty Xa; // posiciones en x de ambas
											// javalinas
	private final SimpleIntegerProperty Xb;

	private final SimpleIntegerProperty Ya; // posiciones en y de ambas
											// javalinas
	private final SimpleIntegerProperty Yb;

	private final String hora;
	private final SimpleDateFormat date = new SimpleDateFormat(
			"dd-MM-yyyy HH:mm:ss", Locale.US);

	/**
	 * Almacenamiento de los diferentes datos de temperatura y su ubicaci�n la
	 * hora esta dada por el momento en el que se crea la clase
	 * 
	 * @param x1
	 *            Posicion en x(relativa al vagon) de la primera jabalina
	 * @param y1
	 *            Posicion en y(relativa al vagon) de la primera jabalina
	 * @param dx
	 *            Distancia en x entre la primera y segunda jabalina
	 * @param dy
	 *            Distancia en y entre la primera y segunda jabalina
	 * @param a1
	 *            Temperatura primer sensor primera jabalina
	 * @param a2
	 *            Temperatura segundo sensor primera jabalina
	 * @param a3
	 *            Temperatura tercer sensor primera jabalina
	 * @param a4
	 *            Temperatura cuarto sensor primera jabalina
	 * @param b1
	 *            Temperatura primer sensor segunda jabalina
	 * @param b2
	 *            Temperatura segundo sensor segunda jabalina
	 * @param b3
	 *            Temperatura tercer sensor segunda jabalina
	 * @param b4
	 *            Temperatura cuarto sensor segunda jabalina
	 */
	public Jabalinas(int x1, int y1, int dx, int dy, float a1, float a2,
			float a3, float a4, float b1, float b2, float b3, float b4) {// la
																			// clase
																			// de
																			// momento
																			// asume
																			// que
																			// la
																			// posici�n
																			// dada
																			// esta
																			// dada
																			// respecto
																			// al
																			// vagon
																			// no
																			// a
																			// la
																			// maquina
																			// corregir
																			// eso!!
		// Inicializar variables de posicion
		this.Xa = new SimpleIntegerProperty(x1);
		this.Xb = new SimpleIntegerProperty(x1 + dx);
		this.Ya = new SimpleIntegerProperty(y1);
		this.Yb = new SimpleIntegerProperty(y1 + dy);
		// Inicializar datos de temperatura
		this.T1 = new SimpleFloatProperty[] { new SimpleFloatProperty(a1),
				new SimpleFloatProperty(a2), new SimpleFloatProperty(a3),
				new SimpleFloatProperty(a4) };
		this.T2 = new SimpleFloatProperty[] { new SimpleFloatProperty(b1),
				new SimpleFloatProperty(b2), new SimpleFloatProperty(b3),
				new SimpleFloatProperty(b4) };
		// inicializar datos de hora
		this.hora = date.format(new GregorianCalendar().getTime()).toString();
	}

	public Jabalinas(String parJab) {// creara una jabalina en base a una cadena
										// con formato

		// cadena:<Xa>;<Ya>;<fecha>;a1;a2;a3;a4&<Xb>;<Yb>;<fecha>;b1;b2;b3;b4
		// Inicializar variables de posicion
		String[] jabas = parJab.split("&");
		String jab1 = jabas[0];
		String jab2 = jabas[1];

		String data1[] = jab1.split(";");
		String data2[] = jab2.split(";");
		/*
		 * data1[0]-->Xa data1[1]-->Ya data1[2]-->fecha data1[3]-->a1
		 * data1[4]-->a2 data1[5]-->a3 data1[6]-->a4
		 * 
		 * data2[0]-->Xb data2[1]-->Yb data2[2]-->fecha data2[3]-->b1
		 * data2[4]-->b2 data2[5]-->b3 data2[6]-->b4
		 */

		int x1 = Integer.parseInt(data1[0]);
		int y1 = Integer.parseInt(data1[1]);
		float a1 = Float.parseFloat(data1[3]);
		float a2 = Float.parseFloat(data1[4]);
		float a3 = Float.parseFloat(data1[5]);
		float a4 = Float.parseFloat(data1[6]);

		int x2 = Integer.parseInt(data2[0]);
		int y2 = Integer.parseInt(data2[1]);
		float b1 = Float.parseFloat(data2[3]);
		float b2 = Float.parseFloat(data2[4]);
		float b3 = Float.parseFloat(data2[5]);
		float b4 = Float.parseFloat(data2[6]);
		// inicializar datos de hora
		this.hora = data1[2];

		this.Xa = new SimpleIntegerProperty(x1);
		this.Xb = new SimpleIntegerProperty(x2);
		this.Ya = new SimpleIntegerProperty(y1);
		this.Yb = new SimpleIntegerProperty(y2);
		// Inicializar datos de temperatura
		this.T1 = new SimpleFloatProperty[] { new SimpleFloatProperty(a1),
				new SimpleFloatProperty(a2), new SimpleFloatProperty(a3),
				new SimpleFloatProperty(a4) };
		this.T2 = new SimpleFloatProperty[] { new SimpleFloatProperty(b1),
				new SimpleFloatProperty(b2), new SimpleFloatProperty(b3),
				new SimpleFloatProperty(b4) };

	}

	/**
	 * Funcion que devuelve toda la informacion de la primera jabalina Formato:
	 * x;y;hora;t1;t2;t3;t4
	 * 
	 * @return Cadena con formato que contiene todos los datos
	 */

	public String getJab1() {
		return (Xa.getValue().toString() + ";"
				+ Ya.getValue().toString() + ";" + hora + ";"
				+ Math.round(T1[0].getValue()) + ";"
				+ Math.round(T1[1].getValue()) + ";"
				+ Math.round(T1[2].getValue()) + ";"
				+ Math.round(T1[3].getValue()));
	}

	/**
	 * Funcion que devuelve toda la informacion de la segunda jabalina Formato:
	 * x;y;hora;t1;t2;t3;t4
	 * 
	 * @return Cadena con formato que contiene todos los datos
	 */

	public String getJab2() {
		return (Xb.getValue().toString() + ";"
				+ Yb.getValue().toString() + ";" + hora + ";"
				+ Math.round(T2[0].getValue()) + ";"
				+ Math.round(T2[1].getValue()) + ";"
				+ Math.round(T2[2].getValue()) + ";"
				+ Math.round(T2[3].getValue()));
	}

	/**
	 * Funcion que devuelve la informacion de ambas jabalinas Formato:
	 * x;y;hora;t1;t2;t3;t4&x;y;hora;t1;t2;t3;t4
	 * 
	 * @return Cadena con formato que contiene todos los datos
	 */
	public String getBoth() {
		return getJab1() + "&" + getJab2();
	}
}
