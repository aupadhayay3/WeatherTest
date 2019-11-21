import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * This class is supposed to provide three methods which parse and analyze weather data from two CSV files
 */
public class Tester {

    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);

        //Be sure to input the location of the file along with the fileName.CSV of the Atlanta data specifically
        System.out.println("PASTE FILE PATH FOR ATLANTA WEATHER DATA");
        String atlDataFileName = scan.nextLine();

        //Be sure to input the location of the file along with the fileName.CSV of the Canada data specifically
        System.out.println("PASTE FILE PATH FOR CANADA WEATHER DATA");
        String canDataFileName = scan.nextLine();

        //These File objects can now be passed as arguments into the three methods to interpret their data
        final File ATL_DATA = new File(atlDataFileName);
        final File CAN_DATA = new File(canDataFileName);


        //Some sample test cases (arguments can be altered to test different inputs):
        dryBulbTempAnalysis(CAN_DATA, "1/1/17");
        roundedWindChills(ATL_DATA, "3/23/08");
        mostSimilarDay(CAN_DATA, ATL_DATA);

    }

    /**
     * This method takes a data set and a date as its arguments and returns a data structure with the average
     * and sample standard deviation of the Fahrenheit dry-bulb temperature between the times of sunrise and sunset
     * @param dataSet
     * @param date
     * @return a String array of length 2 consisting of the average as the first element and the sample standard
     * deviation as the second element
     */
    public static String[] dryBulbTempAnalysis(File dataSet, String date) throws IOException {
        int dryTempSum = 0;
        double avg = 0.0;
        double standardDeviation = 0.0;
        int count = 0;
        BufferedReader br = null;
        String line = "";
        String splitBy = ",";
        try {
            br = new BufferedReader(new FileReader(dataSet));
            line = br.readLine();
            int lineNum = 1;
            boolean outlier = false;
            while ((line = br.readLine()) != null) {
                lineNum++;
                String[] dataInLine = line.split(splitBy);
                int end1 = dataInLine[5].indexOf(" ");
                int end2;
                if(dataInLine[10].indexOf("s") == -1){
                    end2 = dataInLine[10].length();
                }else {
                    end2 = dataInLine[10].indexOf("s");
                    BufferedReader outlierChecker = new BufferedReader(new FileReader(dataSet));
                    String line2 = "";
                    outlierChecker.readLine();
                    int count2 = 1;
                    int end3;
                    ArrayList<Integer> values = new ArrayList<Integer>();
                    while((line2 = outlierChecker.readLine()) != null){
                        count2++;
                        String[] dataInLine2 = line2.split(splitBy);
                        if(dataInLine2[10].length() != 0 && dataInLine[5].substring(0, end1).equals(dataInLine2[5].substring(0, dataInLine2[5].indexOf(" ")))){
                            if(dataInLine2[10].indexOf("s") == -1){
                                end3 = dataInLine2[10].length();
                            }else{
                                end3 = dataInLine2[10].indexOf("s");
                            }
                            values.add(Integer.parseInt(dataInLine2[10].substring(0, end3)));
                        }
                    }
                    Collections.sort(values);
                    int min = values.get(0);
                    int max = values.get(values.size() - 1);
                    int median = values.get((values.size() - 1)/2);
                    int q1 = values.get((((values.size() - 1)/2) + 0)/2);
                    int q3 = values.get((((values.size() - 1)/2) + values.size() - 1)/2);
                    int iqr = q3 - q1;
                    double q1range = (double) q1 - (double) iqr*1.5;
                    double q3range = (double) q3 + (double) iqr*1.5;

                    if(Integer.parseInt(dataInLine[10].substring(0, end2)) > q3range ||
                            Integer.parseInt(dataInLine[10].substring(0, end2)) < q1range){
                        outlier = true;
                    }
                }
                //The if-block determines if there is temperature data on the desired date and adds it to the sum
                if(dataInLine[5].substring(0, end1).equals(date) && dataInLine[10].length() != 0) {
                    if(!outlier) {
                        dryTempSum += Integer.parseInt(dataInLine[10].substring(0, end2));
                        count++;
                    }
                    outlier = false;
                }
            }
            //calculates the arithmetic mean
            avg = (dryTempSum)/((double) count);

            double[] meanDifferenceSquared = new double[count];
            double meanDiffSquared = 0.0;
            count = 0;

            br = new BufferedReader(new FileReader(dataSet));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] dataInLine = line.split(splitBy);
                //stops reading the string before the time of day - meaning it only retrieves the date
                int end1 = dataInLine[5].indexOf(" ");
                int end2;
                if(dataInLine[10].indexOf("s") == -1){
                    end2 = dataInLine[10].length();
                }else {
                    end2 = dataInLine[10].indexOf("s");
                }
                if(dataInLine[5].substring(0, end1).equals(date) && dataInLine[10].length() != 0) {
                    meanDiffSquared = Math.pow((avg - Double.parseDouble(dataInLine[10].substring(0, end2))), 2.0);
                    meanDifferenceSquared[count] = meanDiffSquared;
                    count++;
                }
            }
            double sum = 0.0;
            for(int i = 0; i < meanDifferenceSquared.length; i++){
                sum += meanDifferenceSquared[i];
            }
            //calculates the sample standard deviation
            standardDeviation = Math.pow(((1.0/((double) count - 1.0))*sum), 0.5);

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //String array of the average and deviation, respectively, in the 0th and 1st index
        String[] avgAndDeviation = {Double.toString(avg), Double.toString(standardDeviation)};
        return avgAndDeviation;
    }

    /**
     * This method takes a data set and a date as its arguments and returns the wind chills rounded to the nearest
     * integer for the times when the temperature is less than or equal to 40 degrees Fahrenheit
     * @param dataSet
     * @param date
     * @return a String array consisting of a time for wind chills, then the value of the wind chills in an alternating
     * fashion within the array
     */
    public static ArrayList<String> roundedWindChills(File dataSet, String date) throws IOException {
        ArrayList<String> windChills = new ArrayList<String>();
        BufferedReader br = null;
        String line = "";
        String splitBy = ",";
        int count = 0;
        double windChillValue;
        try {
            br = new BufferedReader(new FileReader(dataSet));
            line = br.readLine();
            boolean outlier = false;
            while ((line = br.readLine()) != null) {
                String[] dataInLine = line.split(splitBy);
                int end1 = dataInLine[5].indexOf(" ");
                int end2;
                if(dataInLine[10].indexOf("s") == -1){
                    end2 = dataInLine[10].length();
                }else {
                    end2 = dataInLine[10].indexOf("s");
                    BufferedReader outlierChecker = new BufferedReader(new FileReader(dataSet));
                    String line2 = "";
                    outlierChecker.readLine();
                    int count2 = 1;
                    int end3;
                    ArrayList<Integer> values = new ArrayList<Integer>();
                    while((line2 = outlierChecker.readLine()) != null){
                        count2++;
                        String[] dataInLine2 = line2.split(splitBy);
                        if(dataInLine2[10].length() != 0 && dataInLine[5].substring(0, end1).equals(dataInLine2[5].substring(0, dataInLine2[5].indexOf(" ")))){
                            if(dataInLine2[10].indexOf("s") == -1){
                                end3 = dataInLine2[10].length();
                            }else{
                                end3 = dataInLine2[10].indexOf("s");
                            }
                            values.add(Integer.parseInt(dataInLine2[10].substring(0, end3)));
                        }
                    }
                    Collections.sort(values);
                    int min = values.get(0);
                    int max = values.get(values.size() - 1);
                    int median = values.get((values.size() - 1)/2);
                    int q1 = values.get((((values.size() - 1)/2) + 0)/2);
                    int q3 = values.get((((values.size() - 1)/2) + values.size() - 1)/2);
                    int iqr = q3 - q1;
                    double q1range = (double) q1 - (double) iqr*1.5;
                    double q3range = (double) q3 + (double) iqr*1.5;

                    if(Integer.parseInt(dataInLine[10].substring(0, end2)) > q3range ||
                            Integer.parseInt(dataInLine[10].substring(0, end2)) < q1range){
                        outlier = true;
                    }
                }
                //makes sure the entries are not blank before proceeding
                if(!outlier && dataInLine[5].substring(0, end1).equals(date)  && dataInLine[17].length() != 0 && dataInLine[10].length() != 0 && Integer.parseInt(dataInLine[10].substring(0, end2)) <= 40){
                    double dryTemp = Double.parseDouble(dataInLine[10].substring(0, end2));
                    double windSpeed = Double.parseDouble(dataInLine[17]);

                    //most common formula for calculating wind chill found online
                    windChillValue = 35.74 + 0.6215*(dryTemp) - 35.75*(Math.pow(windSpeed, 0.16)) + 0.4275*(dryTemp)*(Math.pow(windSpeed, 0.16));
                    windChills.add(dataInLine[5].substring(end1 + 1));
                    //rounds value to nearest integer
                    windChills.add(Integer.toString((int) Math.round(windChillValue)));
                }
                outlier = false;
            }

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return windChills;
    }

    /**
     * This method reads both data sets and finds the day in which the conditions in Canadian, TX, were most similar
     * to Atlanta's Hartsfield-Jackson Airport
     * @param dataSet1
     * @param dataSet2
     * @return a String array of length 2 consisting of the two dates where the weather is most similar and where
     * the first element is the date from the first data set and the second element is the date from the second
     * data set
     */
    public static String[] mostSimilarDay(File dataSet1, File dataSet2) throws IOException {
        String[] mostSimilarDates = new String[2];
        BufferedReader br1 = null, br2 = null;
        String line1, line2 = "";
        String splitBy = ",";
        ArrayList<Double> ds1ApparentTemperatureHourly = new ArrayList<Double>();
        ArrayList<Double> ds2ApparentTemperatureHourly = new ArrayList<Double>();
        ArrayList<String> ds1AvgApparentPerDay = new ArrayList<String>();
        ArrayList<String> ds2AvgApparentPerDay = new ArrayList<String>();
        String ds1NewDate = "";
        String ds2NewDate = "";

        try {
            br1 = new BufferedReader(new FileReader(dataSet1));
            br2 = new BufferedReader(new FileReader(dataSet2));
            line1 = br1.readLine();
            line2 = br2.readLine();
            boolean outlier1 = false;
            boolean outlier2 = false;

            while ((line1 = br1.readLine()) != null) {
                String[] dataInLine = line1.split(splitBy);
                int end1 = dataInLine[5].indexOf(" ");
                int end2;
                int end4;
                if(dataInLine[10].indexOf("s") == -1){
                    end2 = dataInLine[10].length();
                }else {
                    end2 = dataInLine[10].indexOf("s");
                    BufferedReader outlierChecker = new BufferedReader(new FileReader(dataSet1));
                    String line3 = "";
                    outlierChecker.readLine();
                    int count2 = 1;
                    int end3;
                    ArrayList<Integer> values = new ArrayList<Integer>();
                    while((line3 = outlierChecker.readLine()) != null){
                        count2++;
                        String[] dataInLine2 = line3.split(splitBy);
                        if(dataInLine2[10].length() != 0 && dataInLine[5].substring(0, end1).equals(dataInLine2[5].substring(0, dataInLine2[5].indexOf(" ")))){
                            if(dataInLine2[10].indexOf("s") == -1){
                                end3 = dataInLine2[10].length();
                            }else{
                                end3 = dataInLine2[10].indexOf("s");
                            }
                            values.add(Integer.parseInt(dataInLine2[10].substring(0, end3)));
                        }
                    }
                    Collections.sort(values);
                    int min = values.get(0);
                    int max = values.get(values.size() - 1);
                    int median = values.get((values.size() - 1)/2);
                    int q1 = values.get((((values.size() - 1)/2) + 0)/2);
                    int q3 = values.get((((values.size() - 1)/2) + values.size() - 1)/2);
                    int iqr = q3 - q1;
                    double q1range = (double) q1 - (double) iqr*1.5;
                    double q3range = (double) q3 + (double) iqr*1.5;

                    if(Integer.parseInt(dataInLine[10].substring(0, end2)) > q3range ||
                            Integer.parseInt(dataInLine[10].substring(0, end2)) < q1range){
                        outlier = true;
                    }
                }
                if(dataInLine[17].indexOf("s") == -1){
                    end4 = dataInLine[17].length();
                }else {
                    end4 = dataInLine[17].indexOf("s");
                }

                //will only average the hourly "apparent temperatures" when it gathers all the hourly temperatures of a single date
                if(!dataInLine[5].substring(0,end1).equals(ds1NewDate)){
                    //makes sure there are values to average
                    if(ds1ApparentTemperatureHourly.size() > 0) {
                        int sum = 0;
                        for (int i = 0; i < ds1ApparentTemperatureHourly.size(); i++) {
                            sum += ds1ApparentTemperatureHourly.get(i);
                        }
                        ds1AvgApparentPerDay.add(Double.toString((double) sum / ds1ApparentTemperatureHourly.size()));
                        //clears array to be refilled again for the next date
                        ds1ApparentTemperatureHourly.clear();
                    }
                    //adds the next different date
                    ds1NewDate = dataInLine[5].substring(0,end1);
                    ds1AvgApparentPerDay.add(ds1NewDate);
                }
                br1.mark(1000);
                //checks if the next line is null, meaning an average of hourly apparent temperatures needs to be taken
                if(br1.readLine() == null){
                    int sum = 0;
                    for(int i = 0; i < ds1ApparentTemperatureHourly.size(); i++){
                        sum += ds1ApparentTemperatureHourly.get(i);
                    }
                    ds1AvgApparentPerDay.add(Double.toString((double) sum/ds1ApparentTemperatureHourly.size()));
                    ds1ApparentTemperatureHourly.clear();
                }
                //makes sure that the pointer goes back to the original position
                br1.reset();

                //the following if-else statements are based on the method for calculating apparent temperature, dependent on dry bulb temperature
                if(dataInLine[17].length() != 0 && dataInLine[10].length() != 0 && Integer.parseInt(dataInLine[10].substring(0, end2)) <= 40){
                    double dryTemp = Double.parseDouble(dataInLine[10].substring(0, end2));
                    double windSpeed = Double.parseDouble(dataInLine[17].substring(0, end4));
                    ds1ApparentTemperatureHourly.add(35.74 + 0.6215*(dryTemp) - 35.75*(Math.pow(windSpeed, 0.16)) + 0.4275*(dryTemp)*(Math.pow(windSpeed, 0.16)));
                }else if(dataInLine[16].length() != 0 && dataInLine[10].length() != 0 && Integer.parseInt(dataInLine[10].substring(0, end2)) >= 81){
                    //this calculates the heat index, which is dependent on values for humidity (which is provided in the data)
                    double dryTemp = Double.parseDouble(dataInLine[10].substring(0, end2));
                    double rHumid = Double.parseDouble(dataInLine[16]);
                    double c1 = -42.38;
                    double c2 = 2.049;
                    double c3 = 10.14;
                    double c4 = -0.2248;
                    double c5 = -0.006838;
                    double c6 = -0.05482;
                    double c7 = 0.001228;
                    double c8 = 0.0008528;
                    double c9 = -0.00000199;
                    ds1ApparentTemperatureHourly.add(c1 + c2*(dryTemp) + c3*(rHumid) + c4*(dryTemp)*(rHumid) + c5*Math.pow(dryTemp, 2) + c6*Math.pow(rHumid, 2)
                            + c7*Math.pow(dryTemp, 2)*(rHumid) + c8*Math.pow(rHumid, 2)*(dryTemp) + c9*Math.pow(dryTemp, 2)*Math.pow(rHumid, 2));
                }else if(dataInLine[10].length() != 0 && Integer.parseInt(dataInLine[10].substring(0, end2)) > 40 && Integer.parseInt(dataInLine[10].substring(0, end2)) < 81){
                    double dryTemp = Double.parseDouble(dataInLine[10].substring(0, end2));
                    ds1ApparentTemperatureHourly.add(dryTemp);
                }


            }

            //does the same procedure for the second data set
            while ((line2 = br2.readLine()) != null) {
                String[] dataInLine = line2.split(splitBy);
                int end1 = dataInLine[5].indexOf(" ");
                int end2;
                int end4;
                if(dataInLine[10].indexOf("s") == -1){
                    end2 = dataInLine[10].length();
                }else {
                    end2 = dataInLine[10].indexOf("s");
                }
                if(dataInLine[17].indexOf("s") == -1){
                    end4 = dataInLine[17].length();
                }else {
                    end4 = dataInLine[17].indexOf("s");
                }

                if(!dataInLine[5].substring(0,end1).equals(ds2NewDate)){
                    if(ds2ApparentTemperatureHourly.size() > 0) {
                        int sum = 0;
                        for (int i = 0; i < ds2ApparentTemperatureHourly.size(); i++) {
                            sum += ds2ApparentTemperatureHourly.get(i);
                        }
                        ds2AvgApparentPerDay.add(Double.toString((double) sum / ds2ApparentTemperatureHourly.size()));
                        ds2ApparentTemperatureHourly.clear();
                    }
                    ds2NewDate = dataInLine[5].substring(0,end1);
                    ds2AvgApparentPerDay.add(ds2NewDate);
                }
                br2.mark(1000);
                if(br2.readLine() == null){
                    int sum = 0;
                    for(int i = 0; i < ds2ApparentTemperatureHourly.size(); i++){
                        sum += ds2ApparentTemperatureHourly.get(i);
                    }
                    ds2AvgApparentPerDay.add(Double.toString((double) sum/ds2ApparentTemperatureHourly.size()));
                    ds2ApparentTemperatureHourly.clear();
                }
                br2.reset();

                if(dataInLine[17].length() != 0 && dataInLine[10].length() != 0 && Integer.parseInt(dataInLine[10].substring(0, end2)) <= 40){
                    double dryTemp = Double.parseDouble(dataInLine[10].substring(0, end2));
                    double windSpeed = Double.parseDouble(dataInLine[17].substring(0, end4));
                    ds2ApparentTemperatureHourly.add(35.74 + 0.6215*(dryTemp) - 35.75*(Math.pow(windSpeed, 0.16)) + 0.4275*(dryTemp)*(Math.pow(windSpeed, 0.16)));
                }else if(dataInLine[16].length() != 0 && dataInLine[10].length() != 0 && Integer.parseInt(dataInLine[10].substring(0, end2)) >= 81){
                    double dryTemp = Double.parseDouble(dataInLine[10].substring(0, end2));
                    double rHumid = Double.parseDouble(dataInLine[16]);
                    double c1 = -42.38;
                    double c2 = 2.049;
                    double c3 = 10.14;
                    double c4 = -0.2248;
                    double c5 = -0.006838;
                    double c6 = -0.05482;
                    double c7 = 0.001228;
                    double c8 = 0.0008528;
                    double c9 = -0.00000199;
                    ds2ApparentTemperatureHourly.add(c1 + c2*(dryTemp) + c3*(rHumid) + c4*(dryTemp)*(rHumid) + c5*Math.pow(dryTemp, 2) + c6*Math.pow(rHumid, 2)
                            + c7*Math.pow(dryTemp, 2)*(rHumid) + c8*Math.pow(rHumid, 2)*(dryTemp) + c9*Math.pow(dryTemp, 2)*Math.pow(rHumid, 2));
                }else if(dataInLine[10].length() != 0 && Integer.parseInt(dataInLine[10].substring(0, end2)) > 40 && Integer.parseInt(dataInLine[10].substring(0, end2)) < 81){
                    double dryTemp = Double.parseDouble(dataInLine[10].substring(0, end2));
                    ds2ApparentTemperatureHourly.add(dryTemp);
                }
            }

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br1 != null) {
                try {
                    br1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (br2 != null) {
                try {
                    br2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //the following code finds the difference between every combination of average apparent temperatures for the dates of the two data sets
        double minimumDifference = Math.abs(Double.parseDouble(ds1AvgApparentPerDay.get(1)) - Double.parseDouble(ds2AvgApparentPerDay.get(1)));
        mostSimilarDates[0] = ds1AvgApparentPerDay.get(0);
        mostSimilarDates[1] = ds2AvgApparentPerDay.get(0);

        for(int i = 0; i < ds1AvgApparentPerDay.size(); i++){
            for(int j = 0; j < ds2AvgApparentPerDay.size(); j++){
                //will find the smallest difference between the average apparent temperatures of dates from the two data sets
                if(ds2AvgApparentPerDay.get(j).indexOf("/") == -1 && ds1AvgApparentPerDay.get(i).indexOf("/") == -1 && Math.abs(Double.parseDouble(ds1AvgApparentPerDay.get(i)) - Double.parseDouble(ds2AvgApparentPerDay.get(j))) < minimumDifference){
                    minimumDifference = Math.abs(Double.parseDouble(ds1AvgApparentPerDay.get(i)) - Double.parseDouble(ds2AvgApparentPerDay.get(j)));
                    mostSimilarDates[0] = ds1AvgApparentPerDay.get(i - 1);
                    mostSimilarDates[1] = ds2AvgApparentPerDay.get(j - 1);
                }
            }
        }

        //returns two dates, one from each data set, that have the most similar average apparent temperatures
        return mostSimilarDates;
    }

}

