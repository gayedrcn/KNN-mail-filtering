import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;

public class knnAlgorithm {
	  public static int trainSpam = 0;
	  public static int testSpam = 0;
	  public static int testClassed = 0;
	  public static int trainClassed = 0;
	  public static int testMails = 0;
	  public static int trainMails = 0;
	 
	  public static ArrayList<String> trainSet = new ArrayList<String>();
	  public static ArrayList<String> testSet = new ArrayList<String>();
	  public static Map<String, Double> trainWords = new HashMap<String, Double>(); //train verisindeki tüm kelimeler
	  public static List<List<Double>> testList = new ArrayList<List<Double>>();
	  public static List<List<Double>> trainLists = new ArrayList<List<Double>>(); 
	  public static ArrayList<Double> setsSimilarity = new ArrayList<Double>(); // kümeler için benzerlik listesi
	  public static ArrayList<Integer> afterClassify = new ArrayList<Integer>(); //spam için 1, ham olan için 0

  public void kNNaccuracy() throws IOException {
    trainSet = read("./train");
    testSet = read("./test"); // test ve train verileri okutulur

    trainMails = trainClassed - trainSpam;
    testMails = testClassed - testSpam; // train ve test verilerindeki spam olmayan mailler
    
    for (int i = 0; i < trainSet.size(); i++) {
      String str = trainSet.get(i);
      String[] strings = str.split(" ");
      count(strings);
      } // train verilerinden trainWord hashmapi oluþturulur

    for (Iterator<Map.Entry<String, Double>> it = trainWords.entrySet().iterator(); 
    		it.hasNext();) {
      Map.Entry<String, Double> entry = it.next();
      if (entry.getValue() <= 50) {
        it.remove();
      }
    }
    for (int i = 0; i < trainSet.size(); i++) {//traindeki her bir kelime countOccurence fonk. gonderilir
      String str = trainSet.get(i);
      String[] strings = str.split(" "); 
      trainLists.add(countOccurence(strings)); //hashmapteki tüm degerleri clonelayip tum double degerleri sýfýr yapar.
     
    }
    for (int i = 0; i < testSet.size(); i++) {

      String str = testSet.get(i);
      String[] strings = str.split(" ");
      testList.add(countOccurence(strings));
    }// test verilerinden trainWord hashmapi oluþturulur
    
    for (int k = 1; k <= 16; k=k+3) {
    	ratioCluster(k); //en yakýn komsular icin benzerlik hesabi yapar ve cosSim listesine ekler
    	System.out.println("\n");
        System.out.print("-->k=" + k +";"+ "\n");
        accuracyCalculater();} //tp, tn, fp, fn degerlerini hesaplar
    }
    
    
  public static void accuracyCalculater() {

	    double tp = 0;
	    double fn = 0;
	    double tn = 0;
	    double fp = 0;	  
	    int counter = 0 ;
	    final File testFolder = new File("./test");
	    for (final File testFile : testFolder.listFiles()) {
	      if (afterClassify.get(counter) == 0 && !testFile.getName().contains("sp")) {tp++;} //ham olarak tahmin ve ham
	      if (afterClassify.get(counter) == 1 && !testFile.getName().contains("sp")) {fn++;} //ham olarak tahmin ama spam 
	      if (afterClassify.get(counter) == 1 && testFile.getName().contains("sp")) {tn++;} //spam olarak tahmin ama ham
	      if (afterClassify.get(counter) == 0 && testFile.getName().contains("sp")) {fp++;} //spam olarak tahmin ve spam
	      counter++;
	    }
	    double[][] matrix=new double[2][2]; //confusion matrix
        matrix[0][0] = tp;
        matrix[0][1] = fp;
        matrix[1][0] = fn;
        matrix[1][1] = tn;
        System.out.println("TP/FP" + "\n" + "FN/TN values respectively; ");
        for (int i = 0; i <= 1; i++) {
        	System.out.println("\n");
        	for (int j = 0; j <= 1; j++) {
        System.out.print(matrix[i][j] + " "); }
        }

        System.out.println("\n");
	    double acc = ((tn+tp) / (double)(afterClassify.size())); //accuracy--> tn+tp/tn+tp+fn+fp
	    System.out.println("Accuracy: %" + acc + " ");
	    double precision = (tp / ( (double)(afterClassify.size()) -(tn+fn))); //prec--> tp/tp + fn 
		System.out.println("Precision: %" + precision  + " ");
		double recall = (tp /( (double)(afterClassify.size())-(tn+ fp))); //rec --> tp/tp + fn
		System.out.println("Recall: %" + recall + "");
		 double x = (recall*100);
		 double y = (precision*100);
		double fscore = ((double)(2*x*y)/(double)(x+y));
		System.out.println("F1-Score: %" + fscore/100 + " "); //f1score --> 2*prec*rec /(prec+rec)
	    afterClassify = new ArrayList<Integer>();

	  }
  public static ArrayList<String> read(String path) throws IOException {

	    ArrayList<String> temp = new ArrayList<String>();
	    final File folder = new File(path);

	    for (final File fileEntry : folder.listFiles()) {
	      BufferedReader br = new BufferedReader(new FileReader(fileEntry)); //geici bir final listesi olustrulur, 
	      if (path.contains("train") && fileEntry.getName().contains("sp")) {trainSpam++;} //train dosyasýnda spam olan maillerin sayisi, 
	      if (path.contains("test") && fileEntry.getName().contains("sp")) {testSpam++;} //test dosyasýnda spam olan maillerin sayisi tutulur.
	      if (path.contains("test")) {testClassed++;} //test maillerinin toplam sayisi ve,
	      if (path.contains("train")) {trainClassed++;} //train maillerinin toplam sayisi hesaplanýr.

	      String classifiedMail = "";
	      String sCurrentLine; //her mail stringe cevirilir
	      while ((sCurrentLine = br.readLine()) != null) {classifiedMail += sCurrentLine;}
	      temp.add(classifiedMail); //her string de gecici listeye eklenir.
	    }
	    return temp;
	  }

	  public static double points(List<Double> a, List<Double> b) {
	    double pointSum = 0; //noktalarin toplamýný tutar
	    for (int i = 0; i < a.size(); i++) {
	      pointSum += a.get(i) * b.get(i); //her noktanýn birbirine olan uzaklýðýný hesaplar
	    }
	    return pointSum;
	  }

	  public static double lenghts(List<Double> a, List<Double> b) {
	    double pointSum2 = 0.0; //test setinde hesaplanan her nokta uzaklýðýný birbiriyle toplar
	    for (int i = 0; i < a.size(); i++) {
	      pointSum2 += a.get(i) * a.get(i);
	    }
	    pointSum2 = Math.pow(pointSum2, .5);//karekok
	    double pointSum3 = 0.0; //train setindeki uzunluklarýn her birini toplar
	    for (int i = 0; i < b.size(); i++) {
	      pointSum3 += b.get(i) * b.get(i);
	    }
	    pointSum3 = Math.pow(pointSum3, .5);
	    double sumsProduct = pointSum2 * pointSum3; //uzunluklar toplamýnýn carpimini tutar.
	    return sumsProduct;
	  }

	  public static Map<String, Double> makeZero(Map<String, Double> wordClone) {
	    for (Map.Entry<String, Double> entry : wordClone.entrySet()) {
	      wordClone.put(entry.getKey(), 0.0);//clone hashmap sýfýrlanýr,
	    }
	    return wordClone;
	  }
	  private static void count(String[] arr) { //kelimeleri sayar
	    for (int i = 0; i < arr.length; i++) {
	    	if (!trainWords.containsKey(arr[i])) {trainWords.put(arr[i], 1.0);}
	    	else if (trainWords.containsKey(arr[i])) {trainWords.put(arr[i], trainWords.get(arr[i]) + 1);}
	    }
	  }

	  private static ArrayList<Double> countOccurence(String[] arr) {
	    Map<String, Double> wordClone = new HashMap<String, Double>(trainWords); //trainWordsteki kelimeleri klonlar
	    wordClone = makeZero(wordClone); //hashmapteki tüm degerleri clonelayip tum double degerleri sýfýr yapar.
	    for (int i = 0; i < arr.length; i++) {
	      if (trainWords.containsKey(arr[i])) {wordClone.put(arr[i], wordClone.get(arr[i]) + 1);}//train verileriyle string yapýlýr
	    }
	    ArrayList<Double> values = new ArrayList<Double>(wordClone.values());
	    return values;
	  }
  public static void ratioCluster(int k) {//en yakýn komsular icin benzerlik hesabi yapar ve cosSim listesine ekler
    for (int i = 0; i < testList.size(); i++) {
      for (int j = 0; j < trainLists.size(); j++) {
        List<Double> x = testList.get(i);
        List<Double> y = trainLists.get(j);
        double cosSim = (points(x, y) / lenghts(x, y)); 
        setsSimilarity.add(cosSim);
      }
      getKNN(k);
      setsSimilarity = new ArrayList<Double>();
    }
  }
  public static void getKNN(int k) {//en yakin komsulari bulmak icin setsSimilarity listesi cagirilir
    List<Double> clone = new ArrayList<Double>(setsSimilarity); //setSimilarity listesinin kopyasi olusturulur.
    List<Integer> indexes = new ArrayList<Integer>();
    for (int i = 0; i < k; i++) { //en yakin k komsu bulunur
      double temp = Collections.max(clone);
      int index = setsSimilarity.indexOf(temp);//en yakin k komsunun indeksi listede tutulur
      indexes.add(index);
      clone.set(index, 0.0);
    }
    int spamCount = 0;
    for (int i = 0; i < indexes.size(); i++) { 
      int x = indexes.get(i);//k komsunun indekslerine bakilir
      if (x <= trainSpam) { //Spam degil,
      } else {
        spamCount++; //Spam
      }
    }
    int emailCount = k - spamCount;//ornegin en yakin 5 komsudan 3 u spam degilse, spam degildir.
    if (spamCount >= emailCount)
      afterClassify.add(1);//Listeye spam olanlar icin 1,
    else
      afterClassify.add(0);//spam olmayanlar icin 0 ekler.

  }
  
}