/*
 * Copyright (C) 2010-2014 Andreas Maier
 * CONRAD is developed as an Open Source project under the GNU General Public License (GPL).
*/
package edu.stanford.rsl.conrad.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import edu.stanford.rsl.conrad.utils.Configuration;

/**
 * Class to read and write VTK vector fields. Geometry information is taken from global Configuration.
 * @author akmaier
 */

public abstract class VTKVectorField {

	/**
	 * Writes a 3D/3D vector field that is stored as a float[] into the file denoted by filename
	 * @param filename the filename
	 * @param motionfield the vector field
	 * @throws IOException happens if file cannot be read.
	 */
	public static void writeToFile3D(String filename, float [] motionfield) throws IOException{
		Configuration config = Configuration.getGlobalConfiguration();
		FileOutputStream fos = new FileOutputStream(filename);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		//write header
		bw.write("# vtk DataFile Version 3.0\n");
		bw.write("VTK File Generated by Insight Segmentation and Registration Toolkit (ITK)\n");
		bw.write("BINARY\n");
		bw.write("DATASET STRUCTURED_POINTS\n");
		bw.write("DIMENSIONS " + config.getGeometry().getReconDimensionX() + " " + config.getGeometry().getReconDimensionY() + " "  + config.getGeometry().getReconDimensionZ()+"\n");
		bw.write("SPACING "+ config.getGeometry().getVoxelSpacingX() + " " +config.getGeometry().getVoxelSpacingY() + " "+ config.getGeometry().getVoxelSpacingZ() +"\n");
		bw.write("ORIGIN "+ config.getGeometry().getOriginX() + " " + config.getGeometry().getOriginY() + " "+ config.getGeometry().getOriginZ() + "\n");
		bw.write("POINT_DATA " + config.getGeometry().getReconDimensionX() * config.getGeometry().getReconDimensionY() * config.getGeometry().getReconDimensionZ() +"\n");
		bw.write("VECTORS vectors float\n");
		bw.flush();
		// write vector field
		DataOutputStream doStream = new DataOutputStream(fos);
		for (int i = 0; i<motionfield.length; i++){
			doStream.writeFloat(motionfield[i]);
		}
		doStream.flush();
		doStream.close();
		fos.close();
	}

	/**
	 * Reads a file and interprets it as a VTK vector field. Note that this reader has only limited capabilities in terms of formats and needs to be extended in the future.
	 * At present only structured float vector data is available. The geometry information is not read, but mapped the the current geometry defined in the global Configuration.
	 * @param filename the filename
	 * @return the vector field as float []
	 * @throws IOException may occur during reading
	 */
	public static float [] readFromFile3D(String filename) throws IOException{
		Configuration config = Configuration.getGlobalConfiguration();
		FileInputStream fos = new FileInputStream(filename);
		BufferedReader bw = new BufferedReader(new InputStreamReader(fos));
		// VTK header has variable size. Thus we need to compute the size of the reader while reading it.
		int read = 0;
		//read header
		read += bw.readLine().length()+1;
		//content: ("# vtk DataFile Version 3.0\n");
		read += bw.readLine().length()+1;
		//content: ("VTK File Generated by Insight Segmentation and Registration Toolkit (ITK)\n");
		String format = bw.readLine();
		read += format.length()+1;
		//content: ("BINARY\n");
		if (!format.toUpperCase().contains("BINARY")){
			throw new RuntimeException("VTKVectorField.readFromFile3D can only read binary data!");
		}
		format = bw.readLine();
		read += format.length()+1;
		//content: ("DATASET STRUCTURED_POINTS\n");
		if (!format.toUpperCase().contains("DATASET STRUCTURED_POINTS")){
			throw new RuntimeException("VTKVectorField.readFromFile3D can only read structured point data!");
		}
		format = bw.readLine();
		read += format.length()+1;
		// content: ("DIMENSIONS " + config.getGeometry().getReconDimensionX() + " " + config.getGeometry().getReconDimensionY() + " "  + config.getGeometry().getReconDimensionZ()+"\n");
		if (!format.toUpperCase().contains("DIMENSIONS")){
			throw new RuntimeException("VTKVectorField.readFromFile3D expects dimensions now!");
		}
		format = bw.readLine();
		read += format.length()+1;
		// content: ("SPACING "+ config.getGeometry().getVoxelSpacingX() + " " +config.getGeometry().getVoxelSpacingY() + " "+ config.getGeometry().getVoxelSpacingZ() +"\n");
		if (!format.toUpperCase().contains("SPACING")){
			throw new RuntimeException("VTKVectorField.readFromFile3D expects spacing now!");
		}
		format = bw.readLine();
		read += format.length()+1;
		// content: ("ORIGIN "+ config.getGeometry().getOriginX() + " " + config.getGeometry().getOriginY() + " "+ config.getGeometry().getOriginZ() + "\n");
		if (!format.toUpperCase().contains("ORIGIN")){
			throw new RuntimeException("VTKVectorField.readFromFile3D expects origin now!");
		}
		format = bw.readLine();
		read += format.length()+1;
		// content: ("POINT_DATA " + config.getGeometry().getReconDimensionX() * config.getGeometry().getReconDimensionY() * config.getGeometry().getReconDimensionZ() +"\n");
		format = bw.readLine();
		read += format.length()+1;
		// content: ("VECTORS vectors float\n")
		bw.close();
		fos = new FileInputStream(filename);
		float [] vectorfield = new float[config.getGeometry().getReconDimensionX()*config.getGeometry().getReconDimensionY()*config.getGeometry().getReconDimensionZ()*3];
		DataInputStream doStream = new DataInputStream(fos);
		// Skip header bytes...
		doStream.skipBytes(read);
		// Read vector field.
		for (int i = 0; i<vectorfield.length; i++){
			vectorfield[i] = doStream.readFloat();
			//System.out.println("data: " + i + " " + vectorfield[i]);
		}
		fos.close();
		return vectorfield;
	}
	
}