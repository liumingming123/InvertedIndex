package com.test.hadoop;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


/**
 * Created by liusong on 2017/6/16.
 */
public class InvertedIndex {
	public static class InvertedIndexMapper extends Mapper<LongWritable,Text,Text,Text>{
		Text t1=new Text();
		Text t2=new Text("1");
		FileSplit fs=new FileSplit();
		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			fs=(FileSplit)context.getInputSplit();
			StringTokenizer split=new StringTokenizer(value.toString());
			while(split.hasMoreTokens()){
				t1.set(split.nextToken()+"=>"+fs.getPath().toString());
				context.write(t1,t2);
			}
		}
	}
	public static class InvertedIndexCombiner extends Reducer<Text,Text,Text,Text>{
		Text t1=new Text();
		Text t2=new Text();
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			int sum=0;
			for(Text t:values){
				sum+=Integer.parseInt(t.toString());
			}
			t1.set(key.toString().split("=>")[0]);
			t2.set(key.toString().split("=>")[1]+"=>"+String.valueOf(sum));
			context.write(t1,t2);

		}
	}
	public static class InvertedIndexReducer extends Reducer<Text,Text,Text,Text>{

		Text t1=new Text();
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			StringBuffer sb= new StringBuffer();
			for(Text t:values){
				sb.append(t.toString());
			}
			t1.set(sb.toString());
			context.write(key,t1);
		}
	}
	public static void main(String []args) throws IOException, ClassNotFoundException, InterruptedException {

		Configuration conf=new Configuration();
		Job job=new Job(conf);
		job.setJarByClass(InvertedIndex.class);
		job.setMapperClass(InvertedIndexMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setCombinerClass(InvertedIndexCombiner.class);
		job.setReducerClass(InvertedIndexReducer.class);

		job.setOutputKeyClass(Text.class);//由于reduce的keyin valuein与keyout valueout类型一致，所以这两句也可不加
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job,new Path(args[0]));
		FileOutputFormat.setOutputPath(job,new Path(args[1]));
		job.waitForCompletion(true);
	}
}
