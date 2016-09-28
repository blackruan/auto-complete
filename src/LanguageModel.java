import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

public class LanguageModel {
	
	public static class Map extends Mapper<LongWritable, Text, Text, Text> {
		int threashold;

		// get the threashold parameter from the configuration
		public void setup(Context context) {
			Configuration conf = context.getConfiguration();
			threashold = conf.getInt("threashold", 5);
		}

		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

			if ((value == null) || (value.toString().trim().length() == 0)) {
				return;
			}
			String line = value.toString().trim();
			
			// split phrase and count
			String[] wordsPlusCount = line.split("\t");
			String[] words = wordsPlusCount[0].split("\\s+");
			int count = Integer.valueOf(wordsPlusCount[wordsPlusCount.length - 1]);

			// if line is null or empty, or incomplete, or count less than threashold
			if ((wordsPlusCount.length < 2) || (count <= threashold)) {
				return;
			}

			// output key and value
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < words.length - 1; i++) {
				sb.append(words[i]).append(" ");
			}
			String outputKey = sb.toString().trim();
			String outputValue = words[words.length - 1];
			if (!((outputKey == null) || (outputKey.length() < 1))) {
				context.write(new Text(outputKey), new Text(outputValue + "=" + count));
			}
		}
	}

	public static class Reduce extends Reducer<Text, Text, DBOutputWritable, NullWritable> {

		int n;

		// get the n parameter from the configuration
		public void setup(Context context) {
			Configuration conf = context.getConfiguration();
			n = conf.getInt("n", 5);
		}

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

			//this -> <is=1000, is book=10>
			
			TreeMap<Integer, List<String>> tm = new TreeMap<Integer, List<String>>(Collections.reverseOrder());
			for (Text val : values) {
				String cur_val = val.toString().trim();
				String word = cur_val.split("=")[0].trim();
				int count = Integer.parseInt(cur_val.split("=")[1].trim());
				if(tm.containsKey(count)) {
					tm.get(count).add(word);
				}
				else {
					List<String> list = new ArrayList<>();
					list.add(word);
					tm.put(count, list);
				}
			}

			Iterator<Integer> iter = tm.keySet().iterator();
			
			for(int j=0 ; iter.hasNext() && j < n; j++) {
				int keyCount = iter.next();
				List<String> words = tm.get(keyCount);
				for(String curWord: words) {
					context.write(new DBOutputWritable(key.toString(), curWord, keyCount), NullWritable.get());
					j++;
				}
			}
		}
	}
}
