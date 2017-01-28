package utilities;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.TreeMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import dataClass.AssignedTypeProc;


public class test {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println(getProcessCpuLoad());
		
		int processors = Runtime.getRuntime().availableProcessors();
		
		long freeMem = Runtime.getRuntime().freeMemory();
		
		long totalMem = Runtime.getRuntime().totalMemory();
		
		long maxMem = Runtime.getRuntime().maxMemory();
		
		
		System.out.println("processors: "+processors);
		System.out.println("totalMem (MB): "+totalMem/1024/1024);
		System.out.println("freeMem (MB): "+freeMem/1024/1024);
		System.out.println("maxMem (MB): "+maxMem/1024/1024);
		
		//////////////////
		AssignedTypeProc assigned;
		
		Map<String, AssignedTypeProc> mapAss = new TreeMap<>();
		
		assigned = new AssignedTypeProc();
		assigned.setTypeProc("ETL");
		assigned.setPriority(1);
		assigned.setMaxThread(3);
		mapAss.put("ETL", assigned);
		
		assigned = new AssignedTypeProc();
		assigned.setTypeProc("FTP");
		assigned.setPriority(2);
		assigned.setMaxThread(2);
		mapAss.put("FTP", assigned);

		assigned = new AssignedTypeProc();
		assigned.setTypeProc("OSP");
		assigned.setPriority(3);
		assigned.setMaxThread(4);
		mapAss.put("OSP", assigned);

		assigned = new AssignedTypeProc();
		assigned.setTypeProc("LOR");
		assigned.setPriority(3);
		assigned.setMaxThread(3);
		mapAss.put("LOR", assigned);

		long distinctPriority = mapAss.entrySet().stream().map(map -> map.getValue().getPriority()).distinct().count();
		long totalPriority = mapAss.entrySet().stream().map(map -> map.getValue().getPriority()).count();
		
		System.out.println("distinct Priority: "+ distinctPriority);
		System.out.println("total Priority: "+ totalPriority);
		
		
	}
	
    private static double getProcessCpuLoad() throws Exception {
        MBeanServer mbs    = ManagementFactory.getPlatformMBeanServer();
        ObjectName name    = ObjectName.getInstance("java.lang:type=OperatingSystem");
        AttributeList list = mbs.getAttributes(name, new String[]{ "ProcessCpuLoad" });
        
        

        if (list.isEmpty())     return Double.NaN;

        Attribute att = (Attribute)list.get(0);
        Double value  = (Double)att.getValue();

        // usually takes a couple of seconds before we get real values
        if (value == -1.0)      return Double.NaN;
        // returns a percentage value with 1 decimal point precision
        return ((int)(value * 1000) / 10.0);
    }

}
