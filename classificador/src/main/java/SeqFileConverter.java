import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;


public class SeqFileConverter {
	
	    public void convert(String inputFileName, String outputDir) throws IOException{
	        Configuration configuration = new Configuration();			
	        FileSystem fs = FileSystem.get(configuration);
	        Writer writer = new SequenceFile.Writer(fs, configuration, new Path(outputDir + "/chunk-0"),
	                Text.class, Text.class);
	        int count = 0;
	        BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
	        Text key = new Text();
	        Text value = new Text();
	        while(true) {
	            String line = reader.readLine();
	            if (line == null) {
	                break;
	            }
	            String[] tokens = line.split("\t", 3);
	            String category = tokens[0];
	            String id = tokens[1];
	            String message = tokens[2];
	            key.set("/" + category + "/" + id);
	            value.set(message);
	            writer.append(key, value);
	            count++;
	        }
	        writer.close();
	        System.out.println("Escritas " + count + " entradas.");
	    }
	    public static void main(String[] args) throws IOException {
			SeqFileConverter conv = new SeqFileConverter();
			conv.convert("/home/rayner/class.dat", "/home/rayner/seqOut");
		}
}
