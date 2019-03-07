import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class TopoCompute {
    public Map<String, Map<String, String>> nodeMap = new HashMap<String, Map<String, String>>();
    public Map<String, Set<String>> nxtMap = new HashMap<String, Set<String>>();
    public Map<String, List<String>> preMap = new HashMap<String, List<String>>();
    public Map<String, Integer> inDegree = new HashMap<String, Integer>();
    public List<String> sortedList = new LinkedList<String>();


    public void addNode(String filePath) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
            String line;
            while ((line=br.readLine())!=null) {
                String[] s = line.split(",");
                if (!nodeMap.containsKey(s[0])) {
                    Map<String, String > kv = new HashMap<String, String>();
                    kv.put(s[1], s[3]);
                    nodeMap.put(s[0], kv);
                }
                if (s[2].length()==0) {
                    continue;
                }
                String toId = s[2].split(" ")[1];
                String fromId = s[2].split(" ")[0];
                if (!nxtMap.containsKey(s[0])) {
                    Set<String > tmp = new HashSet<String>();
                    tmp.add(toId);
                    nxtMap.put(s[0], tmp);
                }
                if (preMap.containsKey(toId)) {
                    preMap.get(toId).add(fromId);
                }
                else {
                    List<String> tmp = new ArrayList<String>();
                    tmp.add(fromId);
                    preMap.put(toId, tmp);
                }
                if (!inDegree.containsKey(toId)) {
                    inDegree.put(toId, 1);
                }
                else {
                    inDegree.put(toId, inDegree.get(toId)+1);
                }
            }
            br.close();
        } catch (Exception e) {
            return;
        }
    }

    public void topoSort() {
        Queue<String> zeroInNode = new LinkedList<String>();
        Map<String, Set<String>> nxtM2 = new HashMap<String, Set<String>>(nxtMap);
        Map<String, Integer> inD2 = new HashMap<String, Integer>(inDegree);
        for (String id: nodeMap.keySet()) {
            if (!inD2.containsKey(id)) {
                zeroInNode.add(id);
            }
        }
        while (!zeroInNode.isEmpty()) {
            String curN = zeroInNode.poll();
            sortedList.add(curN);
            if (zeroInNode.isEmpty() && nxtM2.isEmpty())
                break;
            for (String n: nxtM2.get(curN)) {
                inD2.put(n, inD2.get(n)-1);
                if (inD2.get(n)==0) {
                    zeroInNode.add(n);
                }
            }
            nxtM2.remove(curN);
        }
    }

    public String compute() {
        Map<String, Port> resMap = new HashMap<String, Port>();
        Queue<String> sortedL2 = new LinkedList<String>(sortedList);
        while (!sortedL2.isEmpty()){
            String id = sortedL2.poll();
            String type = nodeMap.get(id).keySet().toString().replace("[","").replace("]","").trim();
            if (type.equals("t1")) { // input
                String inVal = nodeMap.get(id).get("t1").split("=")[1];
                Map<String, String > param = new HashMap<String, String>();
                param.put("param1", inVal);
                resMap.put(id, new InOperator().compute(null, param).get("out1"));
            }
            else if (type.equals("t2")) { // output
                String preId = preMap.get(id).get(0);
                Map<String, Port> input = new HashMap<String, Port>();
                input.put("input1", resMap.get(preId));
                resMap.put(id, new OutOperator().compute(input, null).get("out1"));
            }
            else if (type.equals("t3")) { // +
                String preId1 = preMap.get(id).get(0);
                String preId2 = preMap.get(id).get(1);
                Map<String, Port> input = new HashMap<String, Port>();
                input.put("input1", resMap.get(preId1));
                input.put("input2", resMap.get(preId2));
                resMap.put(id, new SumOperator().compute(input, null).get("out1"));
            }
            else if (type.equals("t4")) { // *
                String preId1 = preMap.get(id).get(0);
                String preId2 = preMap.get(id).get(1);
                Map<String, Port> input = new HashMap<String, Port>();
                input.put("input1", resMap.get(preId1));
                input.put("input2", resMap.get(preId2));
                resMap.put(id, new MulOperator().compute(input, null).get("out1"));
            }
            if (sortedL2.isEmpty()) {
                return resMap.get(id).value;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String path = "/Users/fu/Documents/code/data/process1.csv";
        TopoCompute tp = new TopoCompute();
        tp.addNode(path);
        tp.topoSort();
        String res = tp.compute();
    }

}
