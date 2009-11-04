package edu.gatech.c4g.r4g.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import edu.gatech.c4g.r4g.model.Block;
import edu.gatech.c4g.r4g.model.BlockGraph;

public abstract class Loader {
	
	/**
	 * 
	 * @param source
	 *            FeatureSource obtained from the shapefile of the area to
	 *            redistrict
	 * @param galFile
	 *            GAL file containing contiguity information about the area to
	 *            redistrict
	 */
	public BlockGraph load(
			FeatureSource<SimpleFeatureType, SimpleFeature> source,
			String galFile) {
		BlockGraph bg = new BlockGraph();

		try {
			FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source
					.getFeatures();

			Iterator<SimpleFeature> iterator = collection.iterator();
			try {
				for (Iterator<SimpleFeature> i = collection.iterator(); i
						.hasNext();) {
					SimpleFeature feature = i.next();

					Block b = new Block(feature);
					bg.blockTable.put(b.getId(), b);
				}
			} finally {
				collection.close(iterator);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Read GAL file
		parseGal(bg,galFile);

		return bg;

	}

	private void parseGal(BlockGraph bg, String filename) {
		File galFile = new File(filename);

		try {
			FileInputStream fstream = new FileInputStream(galFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			int n_blocks = Integer.parseInt(br.readLine());

			String currentline;
			int line_num = 2;

			for (int i = 0; i < n_blocks; i++, line_num++) {
				// read current block id and number of neighbors
				currentline = br.readLine();

				if (currentline == null) {
					System.err.println("Error in GAL file at line " + line_num);
					System.exit(1);
				}

				StringTokenizer block_st = new StringTokenizer(currentline);
				int current_block_id = -1;
				int num_neighbors = -1;

				try {
					current_block_id = Integer.parseInt(block_st.nextToken());
					num_neighbors = Integer.parseInt(block_st.nextToken());
				} catch (NoSuchElementException e) {
					System.err.println("Error in GAL file at line " + line_num);
					System.exit(1);
				}

				Block currentBlock = bg.blockTable.get(new Integer(
						current_block_id));

				// now read neighbors ids
				currentline = br.readLine();

				if (currentline == null) {
					System.err.println("Error in GAL file at line " + line_num);
					System.exit(1);
				}

				StringTokenizer neighbor_st = new StringTokenizer(currentline);

				for (int j = 0; j < num_neighbors; j++) {
					int neighbor_id = -1;
					try {
						neighbor_id = Integer.parseInt(neighbor_st.nextToken());
					} catch (NoSuchElementException e) {
						System.err.println("Error in GAL file at line "
								+ line_num);
						System.exit(1);
					}

					// System.out.println("Adding " + neighbor_id +
					// " to the neighbors of " + current_block_id);
					currentBlock.neighbors.add(bg.blockTable.get(new Integer(
							neighbor_id)));
				}
			}

			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
