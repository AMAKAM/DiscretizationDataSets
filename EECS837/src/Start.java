
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Start  
{
	private static int Attributes_Count=0;
	private static int Decision_Count=1;
	private static int rows=0;
	private static int number_of_scans;
	private static File file_name=null;
	private static HashSet<Integer> consider_all = new  HashSet<Integer>();
	
	private static ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();  
	private static ArrayList<ArrayList<String>> Decision = new ArrayList<ArrayList<String>>();  
	private static ArrayList<ArrayList<Double>> cutpoints_added = new ArrayList<ArrayList<Double>>();
	private static StringBuffer removed_elements= new StringBuffer();
	private static ArrayList<ArrayList<Double>> redundant_cutpoints = new ArrayList<ArrayList<Double>>();
	private static ArrayList<ArrayList<Double>> final_cutpoints = new ArrayList<ArrayList<Double>>();
	
	private static ArrayList<Double> min_attribute = new ArrayList<Double>();
	private static ArrayList<Double> max_attribute = new ArrayList<Double>();
	private static HashSet<Integer> is_attribute_numerical = new HashSet<Integer>();
	private static ArrayList<String> Column_String =new ArrayList<String>();
	
	private static ArrayList<StringBuffer> OutputBuffer = new ArrayList<StringBuffer>();
	private static ArrayList<ArrayList<StringBuffer>> String_Cutpoints_range  = new ArrayList<ArrayList<StringBuffer>>();

	
	//private static String table_data[][]=null;

	//table only has data of attributes 
	//decision only has decision

	
	
	
	
	/* Check if Input file exists and call the function for tabulating data
	*/		
	private static void Initialize_file_and_tabulate() throws Exception
	{
	
		try{		
			//System.out.println(get_file_name());
			file_name=new File(get_file_name());
			Scanner file_pointer= new Scanner(file_name);
			if(file_name.exists())Tabulate_Data(file_pointer);	
		    }
			catch(Exception e)
			{
				System.out.println("Cannot open/access file");
			}
		
	}
	
	
	private static String get_file_name()
	{
		System.out.println("Enter the input file name");
		String file_name=null;
		boolean  file_can_be_createn=true;
		String g;
		do
		{
			System.out.println("Please enter a valid file name");
			Scanner input_recieve = new Scanner(System.in);
			g=input_recieve.next();
			Pattern p=Pattern.compile("[A-Za-z0-9_]*");
			Matcher matcher=p.matcher(g);
			System.out.println(matcher.matches());
			if(matcher.matches())
				file_can_be_createn=false;
		}while(file_can_be_createn);
		return g;
	}
	
	
	/*  Tabulate the data and assign values
	 * 	assigned values for Attributes_Count 
	 * 						Decision_Count 
	 * 						Column_String
	 * 						Decision  
	 *  assigned values for table
	 */
	private static void Tabulate_Data(Scanner scan) throws Exception
	{
	    try{	
			System.out.println("Started Reading File");
			String x = null;
			scan.next();                     //to ignore " < "
			while(scan.hasNext())
			{	
				x=scan.next();
				if(x.equalsIgnoreCase("d"))
					break;
				Attributes_Count++;
		  	}
			
			while(scan.hasNext())
	  		{
	  			x=scan.next();
	  			if(x.equalsIgnoreCase(">"))break;
				Decision_Count++;			  			
	  		}
	  		System.out.println("Numnber of Attributes "+Attributes_Count);
			System.out.println("Numnber of Decision "+Decision_Count); 
			   
		   scan.next();   						// to ignore  "["	
		   ///Block for reading Attribute String
		   { 
				   for(int i=0;i<(Attributes_Count+Decision_Count);i++)
				   { 
					   Column_String.add(scan.next());
				   }   
			   scan.next(); 							//to ignore " ] "					
		   }
		   
		   while(scan.hasNext())
		   {
			   rows++; 
			   /////BLOCK  to read data
			   { 
				   ArrayList<String> temp_list= new ArrayList<String>();
				   for(int i=0;i<Attributes_Count;i++)
				   { 
					   temp_list.add(scan.next());
				   }
				   table.add(temp_list);  
			   }
			 
			   /////BLOCK to read decision
			   { 
				  ArrayList<String> temp_list= new ArrayList<String>(); 
				  for(int i=0;i<Decision_Count;i++)
				 	{
					  temp_list.add(scan.next());
				 	}
				  Decision.add(temp_list);
				}			
			}
		   
		   
		   if(scan.hasNext())
			   System.out.println("Something is wrong");
		   else
			   System.out.println("Finished Reading");
		   System.out.println("Number of Rows"+rows);		   
		   
	    }
		catch(Exception e)
		{
            e.printStackTrace();
			System.out.println("Error when reading file.");
		}
	}
	

	
	/* 	Find out if attributes are numerical or not
	 * 
	 */
	private static void calculate_is_numeric_attributes()
	{
		int i=0,j=0;
		try{
		Pattern p = Pattern.compile("(^(((-)|(\\+)){0,1}))(\\d+)(((\\.)?(\\d+)){0,1}$)");
		boolean for_loop_breaking;
		for(j=0;j<Attributes_Count;j++)
		{
			for_loop_breaking=false;
			for(i=0;i<rows && !for_loop_breaking; i++)
			{
				Matcher match =p.matcher(table.get(i).get(j));
				if(!match.find())
					for_loop_breaking=true;
			}
			if(!for_loop_breaking)
				is_attribute_numerical.add(j);
		}
		}
		catch(Exception e)
		{
			System.out.println("I "+i+"\nJ "+j+"\nRows"+rows+"\nAttributes_Count "+Attributes_Count);
		}
		}


	private static void discretize()
	{
		
		HashSet<Integer> Discretized = new  HashSet<Integer>();
	
		
		if(number_of_scans-->0)
			MultipleScanning(consider_all);
		else	
			dominant_attribute(consider_all);

		
		boolean perfect = false;
		while(!perfect)
		for(int i=0;i<rows;i++)
		{
			if(!Discretized.contains(i))
			{
				boolean inconsistent=false;
				HashSet<Integer> temp_set = new HashSet<Integer>(); 
				temp_set.add(i);				
				ArrayList<StringBuffer> buffer= to_String_Buffer (cutpoints_added,Discretized);				
				StringBuffer temp_buffer= buffer.get(i);
				String temp_decision = Decision.get(i).get(0);
				for(int x=0;x<rows;x++)
					if(!Discretized.contains(x))
						{						
						if(temp_buffer.toString().compareTo(buffer.get(x).toString())==0)
							{
							if(!temp_decision.equals((Decision.get(x).get(0))))
								{
									inconsistent=true;
									temp_set.add(x);
								}
							else
								{
									temp_set.add(x);
								}
							}
						}
				if(inconsistent)					
					{
					if(number_of_scans-->0)
						MultipleScanning(temp_set);
					else	
						dominant_attribute(temp_set);
					i=-1;
					}
				else
					Discretized.addAll(temp_set);				
				if(Discretized.size()==rows)
					perfect=true;
			}
		}
	
	}
	
	
	
	
	
	private static ArrayList<StringBuffer> to_String_Buffer(ArrayList<ArrayList<Double>> cutpoint_present,HashSet<Integer> temp_discretized)
	{
		ArrayList<ArrayList<StringBuffer>>  temp_string_buffer = new ArrayList<ArrayList<StringBuffer>>();
		ArrayList<StringBuffer> String_Buffer  = new ArrayList<StringBuffer>();
		ArrayList<ArrayList<Integer>> range = new  ArrayList<ArrayList<Integer>>();
		for(int j=0;j<Attributes_Count;j++)
		{
			
			if(is_attribute_numerical.contains(j) && cutpoint_present.get(j).size()>0)
			{
				ArrayList<Double> min = new ArrayList<Double>();
				ArrayList<Double> max = new ArrayList<Double>();
				ArrayList<StringBuffer> temp_string = new ArrayList<StringBuffer>();				
				ArrayList<StringBuffer> temp_string_cutpoint = new ArrayList<StringBuffer>();							
				ArrayList<Integer> temp_range = new ArrayList<Integer>();
				int size = cutpoint_present.get(j).size();
				for(int m=0;m<size;m++)
				{
					if(m==0)
						{
						min.add(m,find_min(j));
						max.add(m,cutpoint_present.get(j).get(m));
						}
					else
						{
						min.add(m,cutpoint_present.get(j).get(m-1));
						max.add(m,cutpoint_present.get(j).get(m));
						}				
				}
				min.add(size,cutpoint_present.get(j).get(size-1));
				max.add(size,find_max(j));
				
				for(int m=0;m<=size;m++)				
					temp_string_cutpoint.add(new StringBuffer(min.get(m).toString()+".."+max.get(m).toString()));
			
				for(int i=0;i<rows;i++)
					{
							double value = Double.parseDouble(table.get(i).get(j));
							int temperory_range = value_of_range(value,min,max);
							temp_range.add(new Integer(temperory_range));
							temp_string.add(new StringBuffer(temp_string_cutpoint.get(temperory_range)));
					}
				range.add(temp_range);
				temp_string_buffer.add(new ArrayList<StringBuffer>(temp_string));
			}
			else if(cutpoint_present.get(j).size()==0)
			{
				ArrayList<StringBuffer> temp_string = new ArrayList<StringBuffer>();
				for(int i=0;i<rows;i++)
					temp_string.add(i,new StringBuffer(find_min(j)+".."+find_max(j)));						
				temp_string_buffer.add(new ArrayList<StringBuffer>(temp_string));
			}
			else
			{
				ArrayList<StringBuffer> temp_string = new ArrayList<StringBuffer>();								
				for(int i=0;i<rows;i++)
					temp_string.add(i,new StringBuffer(table.get(i).get(j)));						
				range.add(null);
				temp_string_buffer.add(new ArrayList<StringBuffer>(temp_string));
			}			
		}
	
		for(int i=0;i<rows;i++)
		{	
			StringBuffer temp_buffer = null;
			for(int j=0;j<Attributes_Count;j++)
			{
				if(temp_buffer==null)
					temp_buffer = new StringBuffer(temp_string_buffer.get(j).get(i).toString());
				else
					temp_buffer = new StringBuffer((temp_buffer.toString() + temp_string_buffer.get(j).get(i).toString()));
			}
			String_Buffer.add(temp_buffer);
		}
	
		return String_Buffer;
	}

	
	
	private static ArrayList<StringBuffer> to_String_Buffer(ArrayList<ArrayList<Double>> cutpoint_present,HashSet<Integer> temp_discretized,char s)
	{
		ArrayList<ArrayList<StringBuffer>>  temp_string_buffer = new ArrayList<ArrayList<StringBuffer>>();
		ArrayList<StringBuffer> String_Buffer  = new ArrayList<StringBuffer>();
		ArrayList<ArrayList<Integer>> range = new  ArrayList<ArrayList<Integer>>();
		for(int j=0;j<Attributes_Count;j++)
		{
			if(is_attribute_numerical.contains(j) && cutpoint_present.get(j).size()>0)
			{
				ArrayList<Double> min = new ArrayList<Double>();
				ArrayList<Double> max = new ArrayList<Double>();
				ArrayList<StringBuffer> temp_string = new ArrayList<StringBuffer>();				
				ArrayList<StringBuffer> temp_string_cutpoint = new ArrayList<StringBuffer>();	
				
				ArrayList<Integer> temp_range = new ArrayList<Integer>();
				int size = cutpoint_present.get(j).size();
				for(int m=0;m<size;m++)
				{
					if(m==0)
						{
						min.add(m,find_min(j));
						max.add(m,cutpoint_present.get(j).get(m));
						}
					else
						{
						min.add(m,cutpoint_present.get(j).get(m-1));
						max.add(m,cutpoint_present.get(j).get(m));
						}				
				}
				min.add(size,cutpoint_present.get(j).get(size-1));
				max.add(size,find_max(j));
				
				for(int m=0;m<=size;m++)				
					temp_string_cutpoint.add(new StringBuffer(min.get(m).toString()+".."+max.get(m).toString()));
			
				for(int i=0;i<rows;i++)
					{
							double value = Double.parseDouble(table.get(i).get(j));
							int temperory_range = value_of_range(value,min,max);
							temp_range.add(new Integer(temperory_range));
							temp_string.add(new StringBuffer(temp_string_cutpoint.get(temperory_range)));
					}
				range.add(temp_range);
				temp_string_buffer.add(new ArrayList<StringBuffer>(temp_string));
				String_Cutpoints_range.add(new ArrayList<StringBuffer>(temp_string_cutpoint));
			}
			else if(cutpoint_present.get(j).size()==0)
			{
				ArrayList<StringBuffer> temp_string = new ArrayList<StringBuffer>();
				ArrayList<StringBuffer> temp_string_cutpoint = new ArrayList<StringBuffer>();	
				for(int i=0;i<rows;i++)
					temp_string.add(i,new StringBuffer(find_min(j)+".."+find_max(j)));			
				temp_string_cutpoint.add(temp_string.get(0));
				temp_string_buffer.add(new ArrayList<StringBuffer>(temp_string));
				String_Cutpoints_range.add(new ArrayList<StringBuffer>(temp_string_cutpoint));
			}
			else
			{
				ArrayList<StringBuffer> temp_string = new ArrayList<StringBuffer>();								
				for(int i=0;i<rows;i++)
					temp_string.add(i,new StringBuffer(table.get(i).get(j)));						
				range.add(null);
				temp_string_buffer.add(new ArrayList<StringBuffer>(temp_string));
				String_Cutpoints_range.add(null);
			}			
		}
	
		for(int i=0;i<rows;i++)
		{	
			StringBuffer temp_buffer = null;
			for(int j=0;j<Attributes_Count;j++)
			{
				if(temp_buffer==null)
					temp_buffer = new StringBuffer(temp_string_buffer.get(j).get(i).toString());
				else
					temp_buffer = new StringBuffer((temp_buffer.toString() +"\t\t"+ temp_string_buffer.get(j).get(i).toString()));
			}
			temp_buffer = new StringBuffer((temp_buffer.toString() +"\t\t"+ Decision.get(i).get(0).toString()));
			String_Buffer.add(temp_buffer);
		}
	
		return String_Buffer;
	}

	
	private static int value_of_range(double temp_value,ArrayList<Double> temp_min,ArrayList<Double> temp_max)
	{
		int return_range=-1;
		for(int m=0;m<temp_min.size();m++)
			if(temp_value >= temp_min.get(m) && temp_value <= temp_max.get(m))
				{
				return_range=m;
				break;
				}
		return return_range;
	}
	
	
	private static double find_max(int row)
	{
		double temp = Double.parseDouble(table.get(0).get(row));
		for(int i=0;i<rows;i++)
			if(Double.parseDouble(table.get(i).get(row))>temp)
				temp=Double.parseDouble(table.get(i).get(row));				
		return temp;
	}
	
	
	
	private static double find_min(int row)
	{
		double temp =Double.parseDouble(table.get(0).get(row));
		for(int i=0;i<rows;i++)
			if(Double.parseDouble(table.get(i).get(row))<temp)
				temp=Double.parseDouble(table.get(i).get(row));				
		return temp;
	}
	
	
	private static void calculate_min_and_max_attribute()
	{
		for(int j=0;j<Attributes_Count;j++)
		{
		 if(is_attribute_numerical.contains(j))	
			{
			 double min= Double.parseDouble(table.get(0).get(j));
			 double max = min;
			 for(int i=0;i<rows;i++)
				{
				 double value = Double.parseDouble(table.get(i).get(j));
				 if(value < min)
					 min= value;
				 if(value > max)
					 min= value;	 
				}
			 min_attribute.add((double)min);
			 max_attribute.add((double)max);
			 }
		 else
		 	{
			 max_attribute.add(null);
			 min_attribute.add(null);
		 	}
		}
	}	
	
	
	
	private static void MultipleScanning(Set<Integer> considerarray)
	{
	ArrayList<ArrayList<Double>> list_of_no_dup_attribute= new ArrayList<ArrayList<Double>>();
	ArrayList<ArrayList<String>> list_of_no_dup_decision= new ArrayList<ArrayList<String>>();
	ArrayList<ArrayList<Double>> cutpoints= new ArrayList<ArrayList<Double>>();
	
	//block to compute no_dup_values_for_attributes	
		for (int j=0;j<Attributes_Count;j++)
		{	
			LinkedHashSet<Double> temp_no_dup=null;
			if(is_attribute_numerical.contains(j))
			{		
				temp_no_dup = new LinkedHashSet<Double>();
				for (int i=0;i<rows;i++)
				{
					if(considerarray.contains(i))
					if(!temp_no_dup.contains(Double.parseDouble(table.get(i).get(j))))
						temp_no_dup.add(Double.parseDouble(table.get(i).get(j)));
				}
			}
			if(temp_no_dup!=null)
				{
				ArrayList<Double> temp_col= new ArrayList<Double>();
				for(Object i:temp_no_dup)
					temp_col.add((Double)i);
				Collections.sort(temp_col);
				list_of_no_dup_attribute.add(j, temp_col);
				}
			else
				list_of_no_dup_attribute.add(j, null);
		}
		
	 System.out.print("list_of_no_dup_attributes");		 	
	 display2D(list_of_no_dup_attribute);
	 //block to compute no_dup_values_for_attributes
	
	//block to compute no_dup_values_for_decisions
		for (int j=0;j<Decision_Count;j++)
		{	
			LinkedHashSet<String> temp_no_dup=null;
				temp_no_dup = new LinkedHashSet<String>();
				for (int i=0;i<rows;i++)
				{
					if(considerarray.contains(i))
					if(!temp_no_dup.contains((Decision.get(i).get(0))))
						temp_no_dup.add(Decision.get(i).get(0));
				}			
			if(temp_no_dup!=null)
				{
				ArrayList<String> temp_col= new ArrayList<String>();
				for(String i:temp_no_dup)
					temp_col.add(i);
				Collections.sort(temp_col);
				list_of_no_dup_decision.add(j, temp_col);
				}
			else
				list_of_no_dup_decision.add(j, null);
		}
	 System.out.print("list_of_no_dup_decision");	
	 display2D(list_of_no_dup_decision);

	 //block to compute no_dup_values_for_decision	
			
	 
	 //block to compute cutpoints from no_dup_values_for_attributes
	   for (int j=0;j<Attributes_Count;j++)
		{
			LinkedHashSet<Double> temp_cutpoints=null;
			if(is_attribute_numerical.contains(j))
			{		
				temp_cutpoints = new LinkedHashSet<Double>();
				for (int i=0;i<list_of_no_dup_attribute.get(j).size()-1;i++)
				{
					double cutpoint=(double)(list_of_no_dup_attribute.get(j).get(i)+list_of_no_dup_attribute.get(j).get(i+1))/2;
				    cutpoint=Math.round(cutpoint*1000);
				    cutpoint=cutpoint/1000;
					if(cutpoints_added.get(j)==null)
						temp_cutpoints.add(cutpoint);
					else if(!cutpoints_added.get(j).contains(cutpoint))
						temp_cutpoints.add(cutpoint);
				}
			}
			if(temp_cutpoints!=null)
				{
				ArrayList<Double> temp_col= new ArrayList<Double>();
				for(Object i:temp_cutpoints)
					temp_col.add((Double)i);
				cutpoints.add(j, temp_col);
				}
			else
				cutpoints.add(j, null);	
		}
	  System.out.print("list_of_cutpoints");	
	  display2D(cutpoints);	    
	 //block to compute cutpoints from no_dup_values_for_attributes		
	 
	 
	 //block to compute Entropy for cutpoints 
	 	
	 for (int j=0;j<Attributes_Count;j++)
		{		
		 	if(is_attribute_numerical.contains(j))
			{	
				ArrayList<ArrayList<HashMap<String,Integer>>> temp_map = new ArrayList<ArrayList<HashMap<String,Integer>>>();
				ArrayList<ArrayList<Integer>> total_element_for_that_cutpoint_for_m = new ArrayList<ArrayList<Integer>>(); 
				ArrayList<ArrayList<Double>> min = new ArrayList<ArrayList<Double>>();
				ArrayList<ArrayList<Double>> max = new ArrayList<ArrayList<Double>>();					
				ArrayList<ArrayList<Double>> temp_cutpoints_consideration = new ArrayList<ArrayList<Double>>();
			
				
				HashMap<String,Integer> temperory_map = new HashMap<String,Integer>();
				for(int m=0;m<list_of_no_dup_decision.get(0).size();m++)
			    	temperory_map.put(list_of_no_dup_decision.get(0).get(m),0);
				
				for(int z=0;z<cutpoints.get(j).size();z++)
					{
						ArrayList<HashMap<String,Integer>> temperory_map_for_ranges_in_cutpoints = new ArrayList<HashMap<String,Integer>>();
						ArrayList<Double> temperory_cutpoints_consideration = new ArrayList<Double>();
						ArrayList<Integer> temp_total = new ArrayList<Integer>();
						if(cutpoints_added.get(j)!=null)
							temperory_cutpoints_consideration.addAll(cutpoints_added.get(j));
						temperory_cutpoints_consideration.add(cutpoints.get(j).get(z));
						Collections.sort(temperory_cutpoints_consideration);
						for(int m=0;m<temperory_cutpoints_consideration.size();m++)
							{
								temperory_map_for_ranges_in_cutpoints.add(new HashMap<String,Integer>(temperory_map));
								temp_total.add(0);
							}
						
						//ArrayList<Double> unmodifiablelist = Collections.unmodifiableList(ArrayList<Double>)
						temp_cutpoints_consideration.add(z,new ArrayList<Double>(temperory_cutpoints_consideration));
						total_element_for_that_cutpoint_for_m.add(z, temp_total); 
						temp_map.add(z,new ArrayList<HashMap<String,Integer>>(temperory_map_for_ranges_in_cutpoints));
					}
				for (int z=0;z<cutpoints.get(j).size();z++)
				{		
					ArrayList<Double> minimum = new ArrayList<Double>();
					ArrayList<Double> maximum = new ArrayList<Double>();
					for(int m=0;m<temp_cutpoints_consideration.get(z).size();m++)
					{					
					if(temp_cutpoints_consideration.get(z).size()==1)
					{						
						minimum.add(0,null);
						maximum.add(0, temp_cutpoints_consideration.get(z).get(m));
						min.add(z,new ArrayList<>(minimum));
						max.add(z,new ArrayList<>(maximum));
					}
					else
					{	
					if(m!=0 && m!=temp_cutpoints_consideration.get(z).size()-1) 
						{
							minimum.add(temp_cutpoints_consideration.get(z).get(m-1));
							maximum.add(temp_cutpoints_consideration.get(z).get(m));
						}
					else if(m==0) 
						{
							minimum.add(null);
							maximum.add(temp_cutpoints_consideration.get(z).get(m));
						}
					else if(m==temp_cutpoints_consideration.get(z).size()-1)    
						{
						  minimum.add(temp_cutpoints_consideration.get(z).get(m));
						  maximum.add(null);
						  min.add(z, new ArrayList<>(minimum)); 
						  max.add(z, new ArrayList<>(maximum));
						} 
					}
					}
				}	
				
				ArrayList<ArrayList<Integer>> range = new ArrayList<ArrayList<Integer>>();
					for(int i=0;i<rows;i++)
					{
						if(considerarray.contains(i))
						{	
								ArrayList<Integer> temp_range = new ArrayList<Integer>();
								double data_value=Double.parseDouble(table.get(i).get(j));
									temp_range.addAll(new ArrayList<Integer>(find_in_which_range(data_value,temp_cutpoints_consideration.size(),min,max)));	
									String temp_string= Decision.get(i).get(0);
									for(int z=0;z<cutpoints.get(j).size();z++)
									{
									int range_m=(int)temp_range.get(z);
										if(range_m!=-1)
											{
											    int value=(int) temp_map.get(z).get(range_m).get(temp_string);
											    temp_map.get(z).get(range_m).put(temp_string, ++ value);
												int temp_value=(int)total_element_for_that_cutpoint_for_m.get(z).get(range_m);
												total_element_for_that_cutpoint_for_m.get(z).set(range_m, (Integer)(++temp_value));
											}
									}
								range.add(i,new ArrayList<Integer>(temp_range));
								}
						else
						{
							range.add(i,null);	
						}
			//System.out.println(temp_map);						
			//System.out.println();			
					}
				
				ArrayList <String> temp_decision_string = new ArrayList<String>(list_of_no_dup_decision.size());		
				for (Map.Entry<String, Integer> mapEntry : temperory_map.entrySet()) {
					temp_decision_string.add(mapEntry.getKey());
				}	
				int selected_cutpoint_index = 0 ;
				double selected_value = 2;
				for(int z=0;z<cutpoints.get(j).size();z++)
					{
						double temp_entropy_value = 0; 
						int total = considerarray.size();
						HashMap<String,Integer> new_map = new HashMap<String,Integer>(temperory_map);
						for(int i=0;i<rows;i++)
						{
							if(considerarray.contains(i))
							{
								if(range.get(i).get(z)==-1)
								{
									String temp_decision_value = Decision.get(i).get(0);
									int temp_value_count = new_map.get(temp_decision_value);
									new_map.put(temp_decision_value, ++temp_value_count);
								}
							}
						}
						
						int temp_total_for_last=considerarray.size();
						for(int m=0;m<temp_cutpoints_consideration.get(z).size();m++)
						{
							int sub_total_value =total_element_for_that_cutpoint_for_m.get(z).get(m);
							temp_total_for_last = temp_total_for_last - sub_total_value;
							double sub_value=0;
							for(int y=0;y<list_of_no_dup_decision.get(0).size();y++)
							{
		
								int sub_count_for_range_m = temp_map.get(z).get(m).get(temp_decision_string.get(y));
								if(sub_count_for_range_m!=0){
								double temp_x = (double) ((double)sub_count_for_range_m/(double)sub_total_value);
								sub_value=sub_value+ ( temp_x * (Math.log(temp_x)/Math.log(2)));}
							}										
							
							temp_entropy_value = temp_entropy_value + (sub_total_value*sub_value/total);
							temp_entropy_value = Math.round(temp_entropy_value *1000);
							temp_entropy_value = temp_entropy_value/1000;
						}
						
						for(int y=0;y<list_of_no_dup_decision.get(0).size();y++)
						{
							int sub_total_value = temp_total_for_last ;
							double sub_value=0;
							int sub_count_for_range_m = new_map.get(temp_decision_string.get(y)); 
							if(sub_count_for_range_m!=0){
							double temp_x = (double) ((double)sub_count_for_range_m/(double)sub_total_value);
							sub_value= (temp_x * (Math.log(temp_x)/Math.log(2)));	
							temp_entropy_value = temp_entropy_value + (sub_total_value*sub_value/total);}		
						}
						
						temp_entropy_value = Math.round((-(1.0)*temp_entropy_value *1000));
						temp_entropy_value = temp_entropy_value/1000;
						System.out.println(temp_entropy_value);
						if(selected_value>temp_entropy_value)
							{
							selected_value=temp_entropy_value;
							selected_cutpoint_index=z;	
							}
					}	
				
			ArrayList<Double> temp_list_for_cutpoint_adding=null;
			System.out.println(cutpoints_added.get(j));			
			if(cutpoints_added.get(j)!=null){	
				temp_list_for_cutpoint_adding = new ArrayList <Double>(cutpoints_added.get(j));				
			 if(cutpoints_added.get(j).size()>0)
				 temp_list_for_cutpoint_adding.add(cutpoints.get(j).get(selected_cutpoint_index));	
			 Collections.sort(temp_list_for_cutpoint_adding);
			 cutpoints_added.set(j,new ArrayList<Double>(temp_list_for_cutpoint_adding));
			 System.out.println("Selcted cutpoint "+cutpoints.get(j).get(selected_cutpoint_index)+" for Row " + j);		
			  	}		
			
			}
		}
	 	//block to compute Entropy for cutpoints  
	}	 
	
	
	
	private static void dominant_attribute(Set<Integer> considerarray)
	{
	ArrayList<ArrayList<Double>> list_of_no_dup_attribute= new ArrayList<ArrayList<Double>>();
	ArrayList<ArrayList<String>> list_of_no_dup_decision= new ArrayList<ArrayList<String>>();
	//block to compute no_dup_values_for_attributes	
		
		for (int j=0;j<Attributes_Count;j++)
		{	
			LinkedHashSet<Double> temp_no_dup=null;
			if(is_attribute_numerical.contains(j))
			{		
				temp_no_dup = new LinkedHashSet<Double>();
				for (int i=0;i<rows;i++)
				{
					if(considerarray.contains(i))
					if(!temp_no_dup.contains(Double.parseDouble(table.get(i).get(j))))
						temp_no_dup.add(Double.parseDouble(table.get(i).get(j)));
				}
			}
			if(temp_no_dup!=null)
				{
				ArrayList<Double> temp_col= new ArrayList<Double>();
				for(Object i:temp_no_dup)
					temp_col.add((Double)i);
				Collections.sort(temp_col);
				list_of_no_dup_attribute.add(j, temp_col);
				}
			else
				list_of_no_dup_attribute.add(j, null);
		}
		
	 System.out.print("list_of_no_dup_attributes");		 	
	 display2D(list_of_no_dup_attribute);
	 //block to compute no_dup_values_for_attributes
	
	//block to compute no_dup_values_for_decisions
		for (int j=0;j<Decision_Count;j++)
		{	
			LinkedHashSet<String> temp_no_dup=null;
				temp_no_dup = new LinkedHashSet<String>();
				for (int i=0;i<rows;i++)
				{
					if(considerarray.contains(i))
					if(!temp_no_dup.contains((Decision.get(i).get(0))))
						temp_no_dup.add(Decision.get(i).get(0));
				}			
			if(temp_no_dup!=null)
				{
				ArrayList<String> temp_col= new ArrayList<String>();
				for(String i:temp_no_dup)
					temp_col.add(i);
				Collections.sort(temp_col);
				list_of_no_dup_decision.add(j, temp_col);
				}
			else
				list_of_no_dup_decision.add(j, null);
		}
	 System.out.print("list_of_no_dup_decision");	
	 display2D(list_of_no_dup_decision);
	 //block to compute no_dup_values_for_decision	
	 
	 
	 //block to entropy
	 ArrayList<Integer> Entropy = new ArrayList<Integer>();
	 int total = considerarray.size();
	 int index_selected=-1;
	 double selected_entropy=2;
		
	 for (int j=0;j<Attributes_Count;j++)
		{	
			
		 if(is_attribute_numerical.contains(j))
			{		
			
		 	ArrayList<Double> entropy = new ArrayList<Double>();
		 	ArrayList<HashMap<String,Integer>> temp_map = new ArrayList<HashMap<String,Integer>>();
		 	HashMap<String,Integer> temperory_map = new HashMap<String,Integer>();
		 	ArrayList<Integer> sub_total = new ArrayList<Integer>();		 	
		 	for(int m=0;m<list_of_no_dup_decision.get(0).size();m++)
		    	temperory_map.put(list_of_no_dup_decision.get(0).get(m),0);
			
		 	int number_of_values = list_of_no_dup_attribute.get(j).size();
			for(int m=0;m<number_of_values;m++)
			{
				temp_map.add(new HashMap<String,Integer>(temperory_map));
				sub_total.add(0);
			}
		
		
				for(int i=0;i<rows;i++)
				if(considerarray.contains(i))
				{
					
					String temp_string = Decision.get(i).get(0);
					double value=Double.parseDouble(table.get(i).get(j));
					for(int m=0;m<number_of_values;m++)
					{
						if(value==list_of_no_dup_attribute.get(j).get(m))
						{
							sub_total.set(m,(1+(int)(sub_total.get(m))));
							temp_map.get(m).put(temp_string, (1+(int)temp_map.get(m).get(temp_string)));
							break;
						}						
					}	
				}
				
				double temp_entropy=0;				
				for(int z=0;z<number_of_values;z++)
 				{				
				double temp_sub_entropy = 0;
				int total_sub_block = sub_total.get(z); 
					for(int m=0;m<list_of_no_dup_decision.get(0).size();m++)
					{
						String temp_str = list_of_no_dup_decision.get(0).get(m);
						int temp_val = temp_map.get(z).get(temp_str);
						if(temp_val!=0)
						{
						double x = (double) ((double) temp_val /(double) total_sub_block);
						temp_sub_entropy = temp_sub_entropy + (x*(double) ((double) Math.log(x)/((double) Math.log(2))));
						}
					}
					temp_entropy = temp_entropy + (double)(((double)total_sub_block/(double)total)*temp_sub_entropy);
				 }
				temp_entropy = Math.round((-(1.0)*temp_entropy*1000));
				temp_entropy = (temp_entropy/1000);				
				if(selected_entropy>temp_entropy)
				{
					selected_entropy=temp_entropy;
					index_selected=j;
				}
			}
		}
	 System.out.println("Row selected "+index_selected+" \n calling function to find cutpoint");
	 dominant_attribute_cutpoint(considerarray,index_selected);
	}
	
	
	
	private static HashSet<Integer> conflicts(HashSet<Integer> temp_consider,HashSet<Integer> temp_discretized,ArrayList<ArrayList<Double>> cutpoint_present)
	{
		ArrayList<StringBuffer> buffer = to_String_Buffer(cutpoint_present,temp_discretized); 
		HashSet<Integer> temp_set = new HashSet<Integer>();
		HashSet<Integer> return_empty_set = new HashSet<Integer>();
		boolean conflicting = false;
		for(int i =0;i<rows && conflicting;i++)
			{
			if(temp_consider.contains(i))
				{
				StringBuffer temp_buffer = new StringBuffer(buffer.get(i));
				String temp_decision = Decision.get(i).get(0);  		
				for(int m=0;m<rows;m++)					
					if(temp_consider.contains(m))
					if(temp_buffer.toString().compareTo(buffer.get(i).toString())==0)
					{						
						if(temp_decision.compareTo(Decision.get(m).get(0))!=0)
						{
							temp_set.add(i);						
							conflicting=true;
							temp_set.add(m);
							
						}
						else
						{
							temp_set.add(i);						
							temp_set.add(m);
						}
					}					
				}		
			}
		if (conflicting)
			return temp_set;
		else
			return return_empty_set;
	}
	
	
	
	private static void dominant_attribute_cutpoint(Set<Integer> considerarray,int chosen_attribute)
	{
		ArrayList<ArrayList<Double>> list_of_no_dup_attribute= new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<String>> list_of_no_dup_decision= new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<Double>> cutpoints= new ArrayList<ArrayList<Double>>();
		
		//block to compute no_dup_values_for_attributes	
			for (int j=0;j<Attributes_Count;j++)
			{	
				LinkedHashSet<Double> temp_no_dup=null;
				if(j==chosen_attribute)
				{		
					temp_no_dup = new LinkedHashSet<Double>();
					for (int i=0;i<rows;i++)
					{
						if(considerarray.contains(i))
						if(!temp_no_dup.contains(Double.parseDouble(table.get(i).get(j))))
							temp_no_dup.add(Double.parseDouble(table.get(i).get(j)));
					}
				}
				if(temp_no_dup!=null)
					{
					ArrayList<Double> temp_col= new ArrayList<Double>();
					for(Object i:temp_no_dup)
						temp_col.add((Double)i);
					Collections.sort(temp_col);
					list_of_no_dup_attribute.add(j, temp_col);
					}
				else
					list_of_no_dup_attribute.add(j, null);
			}
			
		 //System.out.print("list_of_no_dup_attributes");		 	
		 //display2D(list_of_no_dup_attribute);
		 //block to compute no_dup_values_for_attributes
		
		//block to compute no_dup_values_for_decisions
			for (int j=0;j<Decision_Count;j++)
			{	
				LinkedHashSet<String> temp_no_dup=null;
					temp_no_dup = new LinkedHashSet<String>();
					for (int i=0;i<rows;i++)
					{
						if(considerarray.contains(i))
						if(!temp_no_dup.contains((Decision.get(i).get(0))))
							temp_no_dup.add(Decision.get(i).get(0));
					}			
				if(temp_no_dup!=null)
					{
					ArrayList<String> temp_col= new ArrayList<String>();
					for(String i:temp_no_dup)
						temp_col.add(i);
					Collections.sort(temp_col);
					list_of_no_dup_decision.add(j, temp_col);
					}
				else
					list_of_no_dup_decision.add(j, null);
			}
		 //System.out.print("list_of_no_dup_decision");	
		 //display2D(list_of_no_dup_decision);

		 //block to compute no_dup_values_for_decision	
				
		 
		 //block to compute cutpoints from no_dup_values_for_attributes
		   for (int j=0;j<Attributes_Count;j++)
			{
				LinkedHashSet<Double> temp_cutpoints=null;
				if(j==chosen_attribute)
				{		
					temp_cutpoints = new LinkedHashSet<Double>();
					for (int i=0;i<list_of_no_dup_attribute.get(j).size()-1;i++)
					{
						double cutpoint=(double)(list_of_no_dup_attribute.get(j).get(i)+list_of_no_dup_attribute.get(j).get(i+1))/2;
					    cutpoint=Math.round(cutpoint*1000);
					    cutpoint=cutpoint/1000;
						if(cutpoints_added.get(j)==null)
							temp_cutpoints.add(cutpoint);
						else if(!cutpoints_added.get(j).contains(cutpoint))
							temp_cutpoints.add(cutpoint);
					}
				}
				if(temp_cutpoints!=null)
					{
					ArrayList<Double> temp_col= new ArrayList<Double>();
					for(Object i:temp_cutpoints)
						temp_col.add((Double)i);
					cutpoints.add(j, temp_col);
					}
				else
					cutpoints.add(j, null);	
			}
		 //System.out.print("list_of_cutpoints");	
		 //display2D(cutpoints);	    
		 //block to compute cutpoints from no_dup_values_for_attributes		
		 
		 
		 //block to compute Entropy for cutpoints 
		 	
		 for (int j=0;j<Attributes_Count;j++)
			{		
				if(j==chosen_attribute)
				{	
					ArrayList<ArrayList<HashMap<String,Integer>>> temp_map = new ArrayList<ArrayList<HashMap<String,Integer>>>();
					ArrayList<ArrayList<Integer>> total_element_for_that_cutpoint_for_m = new ArrayList<ArrayList<Integer>>(); 
					ArrayList<ArrayList<Double>> min = new ArrayList<ArrayList<Double>>();
					ArrayList<ArrayList<Double>> max = new ArrayList<ArrayList<Double>>();					
					ArrayList<ArrayList<Double>> temp_cutpoints_consideration = new ArrayList<ArrayList<Double>>();
				
					
					HashMap<String,Integer> temperory_map = new HashMap<String,Integer>();
					for(int m=0;m<list_of_no_dup_decision.get(0).size();m++)
				    	temperory_map.put(list_of_no_dup_decision.get(0).get(m),0);
					
					for(int z=0;z<cutpoints.get(j).size();z++)
						{
							ArrayList<HashMap<String,Integer>> temperory_map_for_ranges_in_cutpoints = new ArrayList<HashMap<String,Integer>>();
							ArrayList<Double> temperory_cutpoints_consideration = new ArrayList<Double>();
							ArrayList<Integer> temp_total = new ArrayList<Integer>();
							if(cutpoints_added.get(j)!=null)
								temperory_cutpoints_consideration.addAll(cutpoints_added.get(j));
							temperory_cutpoints_consideration.add(cutpoints.get(j).get(z));
							Collections.sort(temperory_cutpoints_consideration);
							for(int m=0;m<temperory_cutpoints_consideration.size();m++)
								{
									temperory_map_for_ranges_in_cutpoints.add(new HashMap<String,Integer>(temperory_map));
									temp_total.add(0);
								}
							
							//ArrayList<Double> unmodifiablelist = Collections.unmodifiableList(ArrayList<Double>)
							temp_cutpoints_consideration.add(z,new ArrayList<Double>(temperory_cutpoints_consideration));
							total_element_for_that_cutpoint_for_m.add(z, temp_total); 
							temp_map.add(z,new ArrayList<HashMap<String,Integer>>(temperory_map_for_ranges_in_cutpoints));
						}
					for (int z=0;z<cutpoints.get(j).size();z++)
					{		
						ArrayList<Double> minimum = new ArrayList<Double>();
						ArrayList<Double> maximum = new ArrayList<Double>();
						for(int m=0;m<temp_cutpoints_consideration.get(z).size();m++)
						{					
						if(temp_cutpoints_consideration.get(z).size()==1)
						{						
							minimum.add(0,null);
							maximum.add(0, temp_cutpoints_consideration.get(z).get(m));
							min.add(z,new ArrayList<>(minimum));
							max.add(z,new ArrayList<>(maximum));
						}
						else
						{	
						if(m!=0 && m!=temp_cutpoints_consideration.get(z).size()-1) 
							{
								minimum.add(temp_cutpoints_consideration.get(z).get(m-1));
								maximum.add(temp_cutpoints_consideration.get(z).get(m));
							}
						else if(m==0) 
							{
								minimum.add(null);
								maximum.add(temp_cutpoints_consideration.get(z).get(m));
							}
						else if(m==temp_cutpoints_consideration.get(z).size()-1)    
							{
							  minimum.add(temp_cutpoints_consideration.get(z).get(m));
							  maximum.add(null);
							  min.add(z, new ArrayList<>(minimum)); 
							  max.add(z, new ArrayList<>(maximum));
							} 
						}
						}
					}	
					
					ArrayList<ArrayList<Integer>> range = new ArrayList<ArrayList<Integer>>();
						for(int i=0;i<rows;i++)
						{
							if(considerarray.contains(i))
							{	
									ArrayList<Integer> temp_range = new ArrayList<Integer>();
									double data_value=Double.parseDouble(table.get(i).get(j));
										temp_range.addAll(new ArrayList<Integer>(find_in_which_range(data_value,temp_cutpoints_consideration.size(),min,max)));	
										String temp_string= Decision.get(i).get(0);
										for(int z=0;z<cutpoints.get(j).size();z++)
										{
										int range_m=(int)temp_range.get(z);
											if(range_m!=-1)
												{
												    int value=(int) temp_map.get(z).get(range_m).get(temp_string);
												    temp_map.get(z).get(range_m).put(temp_string, ++ value);
													int temp_value=(int)total_element_for_that_cutpoint_for_m.get(z).get(range_m);
													total_element_for_that_cutpoint_for_m.get(z).set(range_m, (Integer)(++temp_value));
												}
										}
									range.add(i,new ArrayList<Integer>(temp_range));
									}
							else
							{
								range.add(i,null);	
							}
				//System.out.println(temp_map);						
				//System.out.println();			
						}
					
					ArrayList <String> temp_decision_string = new ArrayList<String>(list_of_no_dup_decision.size());		
					for (Map.Entry<String, Integer> mapEntry : temperory_map.entrySet()) {
						temp_decision_string.add(mapEntry.getKey());
					}	
					int selected_cutpoint_index = 0 ;
					double selected_value = 2;
					for(int z=0;z<cutpoints.get(j).size();z++)
						{
							double temp_entropy_value = 0; 
							int total = considerarray.size();
							HashMap<String,Integer> new_map = new HashMap<String,Integer>(temperory_map);
							for(int i=0;i<rows;i++)
							{
								if(considerarray.contains(i))
								{
									if(range.get(i).get(z)==-1)
									{
										String temp_decision_value = Decision.get(i).get(0);
										int temp_value_count = new_map.get(temp_decision_value);
										new_map.put(temp_decision_value, ++temp_value_count);
									}
								}
							}
							
							int temp_total_for_last=considerarray.size();
							for(int m=0;m<temp_cutpoints_consideration.get(z).size();m++)
							{
								int sub_total_value =total_element_for_that_cutpoint_for_m.get(z).get(m);
								temp_total_for_last = temp_total_for_last - sub_total_value;
								double sub_value=0;
								for(int y=0;y<list_of_no_dup_decision.get(0).size();y++)
								{
			
									int sub_count_for_range_m = temp_map.get(z).get(m).get(temp_decision_string.get(y));
									if(sub_count_for_range_m!=0){
									double temp_x = (double) ((double)sub_count_for_range_m/(double)sub_total_value);
									sub_value=sub_value+ ( temp_x * (Math.log(temp_x)/Math.log(2)));}
								}										
								
								temp_entropy_value = temp_entropy_value + (sub_total_value*sub_value/total);
								temp_entropy_value = Math.round(temp_entropy_value *1000);
								temp_entropy_value = temp_entropy_value/1000;
							}
							
							for(int y=0;y<list_of_no_dup_decision.get(0).size();y++)
							{
								int sub_total_value = temp_total_for_last ;
								double sub_value=0;
								int sub_count_for_range_m = new_map.get(temp_decision_string.get(y)); 
								if(sub_count_for_range_m!=0){
								double temp_x = (double) ((double)sub_count_for_range_m/(double)sub_total_value);
								sub_value= (temp_x * (Math.log(temp_x)/Math.log(2)));	
								temp_entropy_value = temp_entropy_value + (sub_total_value*sub_value/total);}		
							}
							
							temp_entropy_value = Math.round((-(1.0)*temp_entropy_value *1000));
							temp_entropy_value = temp_entropy_value/1000;
							System.out.println(temp_entropy_value);
							if(selected_value>temp_entropy_value)
								{
								selected_value=temp_entropy_value;
								selected_cutpoint_index=z;	
								}
						}	
				ArrayList<Double> temp_list_for_cutpoint_adding=null;
				if(cutpoints_added.get(j)!=null)	
					temp_list_for_cutpoint_adding = new ArrayList <Double>(cutpoints_added.get(j));	
				
				temp_list_for_cutpoint_adding.add(cutpoints.get(j).get(selected_cutpoint_index));	
				Collections.sort(temp_list_for_cutpoint_adding);
				cutpoints_added.set(j,new ArrayList<Double>(temp_list_for_cutpoint_adding));
				System.out.println("Selcted cutpoint from row "+j+" is cutpoint "+cutpoints.get(j).get(selected_cutpoint_index));		
				}
			}		
	}
	
	
	
	private static ArrayList<Integer> find_in_which_range(double value,int no_of_cutpoints,ArrayList<ArrayList<Double>> min_array,ArrayList<ArrayList<Double>> max_array)
	{
		ArrayList<Integer> return_array = new ArrayList<Integer>();
		for(int cutpoint_index=0;cutpoint_index<no_of_cutpoints;cutpoint_index++)
		{			
			int sub_size=min_array.get(cutpoint_index).size();
			for(int m=0;m<sub_size;m++)
			{
				if(sub_size==1)
				{
					if(value<max_array.get(cutpoint_index).get(m))
						return_array.add(0);
					else
						return_array.add(-1);
				}
				else 
				{
					if(m==0)
						{
							if(value<max_array.get(cutpoint_index).get(m))
								return_array.add(m);
						}					
					else if(m==sub_size-1)
						{
							if(value>min_array.get(cutpoint_index).get(m))
								return_array.add(m);
							else
								return_array.add(-1);
						}					
					else if(value<max_array.get(cutpoint_index).get(m) && value>min_array.get(cutpoint_index).get(m))
						{
							return_array.add(m);
						}
				}
			}
		}
	 return return_array;	//error
	}
	
	
	
	
	private static void merge()
	{		
		ArrayList<LinkedHashSet<Double>> consider_cutpoints_set;
		ArrayList<ArrayList<Double>> consider_cutpoints_list;
		ArrayList<LinkedHashSet<Double>> temp = new ArrayList<LinkedHashSet<Double>> (array_to_set(cutpoints_added));
		redundant_cutpoints = new ArrayList<ArrayList<Double>>(set_to_array(temp));
		
		removed_elements=new StringBuffer(" ");
		for(int j=0;j<Attributes_Count;j++)	
		{	
			removed_elements=new StringBuffer(removed_elements.toString()+"\nColumn "+j+" : "+Column_String.get(j)+"\n");
			for(int i=0;i<cutpoints_added.get(j).size();i++)
			{		
				Double temp_double = cutpoints_added.get(j).get(i);
				cutpoints_added.get(j).set(i, null);
				consider_cutpoints_set  = new ArrayList<LinkedHashSet<Double>> (array_to_set(cutpoints_added));												
				consider_cutpoints_list = new ArrayList<ArrayList<Double>>(set_to_array(consider_cutpoints_set));
				if(merge_consistency(consider_cutpoints_list))
					cutpoints_added.get(j).set(i, temp_double);
				else
					removed_elements=new StringBuffer(removed_elements.toString()+temp_double.toString()+"\t\t");
							
			}
		}
		ArrayList<LinkedHashSet<Double>> actual_cutpoints_set = new ArrayList<LinkedHashSet<Double>>();		
		
		actual_cutpoints_set =  new ArrayList<LinkedHashSet<Double>> (array_to_set(cutpoints_added));
		final_cutpoints = new ArrayList<ArrayList<Double>>(set_to_array(actual_cutpoints_set));
		

		OutputBuffer = new ArrayList<StringBuffer>(to_String_Buffer(final_cutpoints,consider_all,'s'));
		System.out.println();

		
		//for(int x=0;x<Attributes_Count;x++)
		//	DisplaySet(try_cutpoints.get(x));
		
		
		// for(int x=0;x<Attributes_Count;x++)
		//	  DisplaySet(try_cutpoints.get(x));
		
		
		
	}

	
	
	private static ArrayList<ArrayList<Double>> set_to_array(ArrayList<LinkedHashSet<Double>> try_cutpoints)
	{
	
		ArrayList<ArrayList<Double>> temp_array = new ArrayList<ArrayList<Double>>();
		for(int j=0;j<Attributes_Count;j++)	
		{	
			ArrayList<Double> temp_list = new ArrayList<Double>();			
			for(Double i:try_cutpoints.get(j))
			{
				temp_list.add(i);
			}
			temp_array.add(new ArrayList<Double>(temp_list));
		}	
		return temp_array;
	}
	
	
	private static ArrayList<LinkedHashSet<Double>> array_to_set(ArrayList<ArrayList<Double>> temp_array)
	{
		ArrayList<LinkedHashSet<Double>> try_cutpoints = new ArrayList<LinkedHashSet<Double>>();	
		for(int j=0;j<Attributes_Count;j++)	
		{	
			LinkedHashSet<Double> temp_set = new LinkedHashSet<Double>();
			
			for(int i=0;i<temp_array.get(j).size();i++)
			{
				if(cutpoints_added.get(j).get(i)!=null)
					temp_set.add(new Double(cutpoints_added.get(j).get(i)));
			}
			try_cutpoints.add(new LinkedHashSet<Double>(temp_set));
		}	
		return try_cutpoints;
	}
	
	
	
	private static boolean merge_consistency(ArrayList<ArrayList<Double>> temp_array)
	{
		boolean inconsistent=false;
		ArrayList<StringBuffer> buffer= to_String_Buffer (temp_array,consider_all);					
		for(int i=0;i<rows;i++)
			{
				StringBuffer temp_buffer= buffer.get(i);
				String temp_decision = Decision.get(i).get(0);
				for(int x=i;x<rows;x++)
						if(temp_buffer.toString().compareTo(buffer.get(x).toString())==0)
							{
							if(!temp_decision.equals((Decision.get(x).get(0))))
								{
									inconsistent=true;
									break;
								}
							}
			}
		return inconsistent;
	}
	
	
	/*just printing out data for debugging 
	 * 
	 */
	private static <T> void display1D(ArrayList<T> s)
	{
		for(int i=0;i<s.size();i++)
			System.out.print(s.get(i)+"\t");
		System.out.println();
	}
	
	
	
	/*just printing out data for debugging 
	 * 
	 */
	private static <T> void display2D(ArrayList<ArrayList<T>> s)
	{
	  System.out.println();
		for(int j=0;j<s.size();j++) 
		{
		System.out.println("Column " +j+"\t\t");	
		for(int i=0;i<s.get(j).size();i++)
			System.out.print(s.get(j).get(i)+"\t");
		System.out.println();
		}
	  System.out.println();
	}


	
	/*
	 * just printing out data for arraylist table and decision for debugging
	 */
	private static <T> void display_table_including_decision(ArrayList<ArrayList<T>> x)
	{
		System.out.println();
		for(int i=0;i<x.size();i++)
		{
			System.out.println("Row "+i);
			for(int j=0;j<x.get(i).size();j++)
			{
				System.out.print(x.get(i).get(j)+"\t");
			}
		System.out.println();	
		}
		System.out.println();		
	}
	
	
	/*just printing out data for debugging 
	 * 
	 */
	private static <T> void DisplaySet(Set<T> x)
	{
		for(Object s:x)
			System.out.print(s+"\t");
		System.out.println();
	}
	
	
	public static void main(String args[]) throws Exception
	{
		
		//number_of_scans=20;
		Scanner scanner=new Scanner(System.in);

		try
		{
		System.out.println("Please Type the number of scans");
		number_of_scans=scanner.nextInt();

		}
		catch(Exception e)
		{
		  System.out.println("Wrong Input");	
		}
		
		
		Initialize_file_and_tabulate();
		calculate_is_numeric_attributes();

		for(int i=0;i<rows;i++)
			consider_all.add(i);
	
		//initialize cutpoints
		for(int i=0;i<Attributes_Count;i++)
			{
			 cutpoints_added.add(new ArrayList<Double>());
			}
		
		
		//the discretization part 
		discretize();
			
			
		
		
		
		System.out.println("Cutpoints");				
		display2D(cutpoints_added);
		
		merge();

		System.out.println("Cutpoints + Redundant removed");				
		display2D(cutpoints_added);	
		
		{
		BufferedWriter x = new BufferedWriter (new FileWriter("test.data"));
		{
			
			x.append("< ");
			for(int i=0;i<Attributes_Count;i++)
			{
				x.append("a ");
			}
			for(int i=0;i<Decision_Count;i++)
			{
				x.append("d ");
			}
			x.append(">");
			x.newLine();
			
			x.append("[ ");	
			for(int i=0;i<Attributes_Count+Decision_Count;i++)
			{
				x.append(Column_String.get(i)+" ");
			}
			x.append("]");
			x.newLine();
			
			for(int i=0;i<OutputBuffer.size();i++)
			{
				x.append(OutputBuffer.get(i));
				x.newLine();
			}
			x.close();
		}
		}
		
		
		
		
		
		{
			BufferedWriter x = new BufferedWriter (new FileWriter("test.int"));	
			x.append("Cutpoint Intervals");
			x.newLine();
			for(int j=0;j<String_Cutpoints_range.size();j++)
			{
															
				x.append("Column "+j+"  --->  "+Column_String.get(j));	
				x.newLine();				
				for(int i=0;i<String_Cutpoints_range.get(j).size();i++)			
				{
					x.append(String_Cutpoints_range.get(j).get(i)+"\t");
				}
				x.newLine();
				
			}
			x.close();
		}
		

		//display_table_including_decision(table);
	    //display1D(Column_String);
	    //display2D(table);
		//display_table_including_decision(Decision);	    
		//display2D(Decision);
	}

}
