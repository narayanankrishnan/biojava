/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.biojava3.survival.kaplanmeier.figure;


import org.biojava3.survival.cox.SurvivalInfo;
import org.biojava3.survival.cox.comparators.SurvivalInfoValueComparator;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author Scooter Willis <willishf at gmail dot com>
 */
public class ExpressionFigure extends JPanel {

    ArrayList<String> title = new ArrayList<String>();
    /**
     *
     */
    public int top;
    /**
     *
     */
    public int bottom;
    /**
     *
     */
    public int left;
    /**
     *
     */
    public int right;
    int titleHeight;
    int xAxisLabelHeight;
    int labelWidth;
    Double maxTime = null;
    Double minX = 0.0;
    Double maxX = 10.0;
    Double minY = 0.0;
    Double maxY = 1.0;
    Double mean = 0.0;
    FontMetrics fm;
    KMFigureInfo kmfi = new KMFigureInfo();
    LinkedHashMap<String, ArrayList<CensorStatus>> survivalData = new LinkedHashMap<String, ArrayList<CensorStatus>>();
    ArrayList<String> lineInfoList = new ArrayList<String>();
    ArrayList<SurvivalInfo> siList = new ArrayList<SurvivalInfo>();
    String variable = "";
    
    private String fileName = "";

    /**
     *
     */
    public ExpressionFigure() {
        super();
        setSize(500, 400);
        setBackground(Color.WHITE);
    }

    /**
     *
     * @param kmfi
     */
    public void setKMFigureInfo(KMFigureInfo kmfi) {
        this.kmfi = kmfi;
        if (kmfi.width != null && kmfi.height != null) {
            this.setSize(kmfi.width, kmfi.height);
        }
    }

    /**
     *
     * @param lineInfoList
     */
    public void setFigureLineInfo(ArrayList<String> lineInfoList) {
        this.lineInfoList = lineInfoList;
        this.repaint();
    }
    

    /**
     *
     * @param title
     * @param _siList
     * @param variable
     */
    public void setSurvivalInfo(ArrayList<String> title, ArrayList<SurvivalInfo> _siList, String variable) {
        this.siList = new ArrayList<SurvivalInfo>();
        this.title = title;
        this.variable = variable;
        
        minX = 0.0;
        maxX = (double)_siList.size();
        minY = 0.0;
        maxY = null;
        DescriptiveStatistics ds = new DescriptiveStatistics();
        for(SurvivalInfo si : _siList){
            this.siList.add(si);
           String v = si.getOriginalMetaData(variable);
           Double value = Double.parseDouble(v);
           ds.addValue(value);
           if(maxTime == null || maxTime < si.getTime()){
               maxTime = si.getTime();
           }

        }
        SurvivalInfoValueComparator sivc = new SurvivalInfoValueComparator(variable);
        Collections.sort(this.siList,sivc);
        mean = ds.getMean();
        minY = ds.getMin();
        maxY = ds.getMax();
        minY = (double)Math.floor(minY);
        maxY = (double)Math.ceil(maxY);
        
        
        this.repaint();
    }
    DecimalFormat df = new DecimalFormat("#.#");

    public void paintComponent(Graphics g) // draw graphics in the panel
    {
        int width = getWidth();             // width of window in pixels
        int height = getHeight();           // height of window in pixels
        setFigureDimensions();
        super.paintComponent(g);            // call superclass to make panel display correctly

        
        drawExpressionLevels(g);
        drawFigureLineInfo(g);
        drawLegend(g);
        // Drawing code goes here
    }

    private void drawFigureLineInfo(Graphics g) {
        g.setColor(Color.BLACK);
        fm = getFontMetrics(getFont());
        int yoffset = fm.getHeight() * lineInfoList.size();

        int x = getX(kmfi.figureLineInfoLowerPercentX * maxX);
        int y = getY(kmfi.figureLineInfoLowerPercentY) - yoffset;

        for (String line : lineInfoList) {
            g.drawString(line, x, y);
            y = y + fm.getHeight();
        }

    }



    private void drawExpressionLevels(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(kmfi.kmStroke);
        g2.setColor(Color.blue);
        Double py = null;
        for(int x = 0; x < siList.size(); x++){
            SurvivalInfo si = siList.get(x);            
            String v = si.getOriginalMetaData(variable);
            Double y = Double.parseDouble(v);
            if(si.getStatus() == 1)
                g2.setColor(Color.RED);
            else
                g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(getX(x), getY((maxY - minY)), getX(x), getY(maxY - y) );
            
            if(py == null)
                py = y;
            if(mean >= py && mean <= y){
                g2.setColor(Color.green);
                g2.drawLine(getX(x), getY(maxY - minY), getX(x), getY(maxY - y ));
                g2.drawLine(getX(x-1), getY(maxY - minY), getX(x-1), getY(maxY - y ));
            }
            py = y;
            
        //    g2.setColor(Color.black);
        //    double yt = getYFromPercentage(1.0 - ((double)x)/((double)siList.size())  );
        //    g2.drawOval(getX(x) - 2, ((int)yt) - 2, 4, 4);
        }
        
    }

    private int getYFromPercentage(double percentage){
        double d = top + (((bottom - top) * percentage));
        return (int) d;
    }
    
    private int getX(double value) {
        double d = left + (((right - left) * value) / (maxX - minX));
        return (int) d;
    }

    private int getY(double value) {
       
        double d = top + (((bottom - top) * value) / (maxY - minY));
        return (int) d;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }



    private void drawLegend(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Font font = g.getFont();
        fm = getFontMetrics(font);
        int fontHeight = fm.getHeight();
        for (int i = 0; i < title.size(); i++) {
            if (fm.stringWidth(title.get(i)) > .8 * this.getWidth()) {

                Font f = new Font(font.getFontName(), Font.PLAIN, 10);
                g.setFont(f);
                fm = getFontMetrics(f);
            }
            g.drawString(title.get(i), (size().width - fm.stringWidth(title.get(i))) / 2, ((i + 1) * fontHeight));
            g.setFont(font);
        }
        // draw the maxY and minY values
        g.drawString(df.format(minY), left - fm.stringWidth(df.format(minY)) - 20, bottom + titleHeight / 6);
        g.drawLine(left - 5, bottom, left, bottom);
        double ySize = maxY - minY;
        double increment = kmfi.yaxisPercentIncrement * ySize;
        increment = Math.ceil(increment);
      //  increment = increment * 10.0;
        double d = minY + increment;
        double graphHeight = top - bottom;
        String label = "";
        while (d < maxY) {
            int yvalue = getY(maxY - d);
            label = df.format(d);
            g.drawString(label, left - (int) ( fm.stringWidth(label)) - 20, yvalue + titleHeight / 6); //

            g.drawLine(left - 5, yvalue, left, yvalue);
            d = d + (increment);
        }

        label = df.format(maxY);
        g.drawString(label, left - (int) ( fm.stringWidth(label)) - 20, top + (titleHeight) / 6);
        g.drawLine(left - 5, top, left, top);

        double timeDistance = maxX - minX;
        double timeIncrement = timeDistance * kmfi.xaxisPercentIncrement;
        double timeInt = (int) timeIncrement;
        if (timeInt < 1.0) {
            timeInt = 1.0;
        }
        double adjustedPercentIncrement = timeInt / timeDistance;

        d = adjustedPercentIncrement; //kmfi.xaxisPercentIncrement;
        while (d <= 1.0) {
            label = df.format((minX * kmfi.timeScale) + d * ((maxX - minX) * kmfi.timeScale)); //
            if (d + adjustedPercentIncrement > 1.0) { //if this is the last one then adjust
                g.drawString(label, left + (int) (d * (right - left)) - (int) (.5 * fm.stringWidth(label)), bottom + fm.getHeight() + 5);
            } else {
                g.drawString(label, left + (int) (d * (right - left)) - (fm.stringWidth(label) / 2), bottom + fm.getHeight() + 5);
            }
            g.drawLine(left + (int) (d * (right - left)), bottom, left + (int) (d * (right - left)), bottom + 5);
            d = d + adjustedPercentIncrement; //kmfi.xaxisPercentIncrement;
        }


        // draw the vertical and horizontal lines
        g2.setStroke(kmfi.axisStroke);
        g.drawLine(left, top, left, bottom);
        g.drawLine(left, bottom, right, bottom);
    }

    private void setFigureDimensions() {
        fm = getFontMetrics(getFont());
        titleHeight = kmfi.titleHeight;//fm.getHeight();
        xAxisLabelHeight = titleHeight;
        labelWidth = Math.max(fm.stringWidth(df.format(minY)),
                fm.stringWidth(df.format(maxY))) + 5;
        top = kmfi.padding + titleHeight;
        bottom = this.getHeight() - kmfi.padding - xAxisLabelHeight;
        left = kmfi.padding + labelWidth;
        right = this.getWidth() - kmfi.padding;

    }

    /**
     *
     * @param fileName
     */
    public void savePNG(String fileName) {
        if(fileName.startsWith("null"))
            return;
        this.fileName = fileName;
        BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = image.createGraphics();

        this.paint(graphics2D);
        try {
            ImageIO.write(image, "png", new File(fileName));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {

            ExpressionFigure expressionFigure = new ExpressionFigure();
  


            JFrame application = new JFrame();
            application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            application.add(expressionFigure);
            expressionFigure.setSize(500, 400);

            application.setSize(500, 400);         // window is 500 pixels wide, 400 high
            application.setVisible(true);

            ArrayList<String> titles = new ArrayList<String>();
            titles.add("Line 1");
            titles.add("line 2");
           
            ArrayList<String> figureInfo = new ArrayList<String>();

            ArrayList<SurvivalInfo> survivalInfoList = new ArrayList<SurvivalInfo>();
            
            for(int i = 0; i < 600; i++){
                double r = Math.random();
                double v = r * 10000;
                double t = Math.random() * 5.0;
                r = Math.random();
                int e = 0;
                if(r < .3)
                    e = 1;
                SurvivalInfo si = new SurvivalInfo(t,e);
                si.addContinuousVariable("META_GENE", v);
                survivalInfoList.add(si);
                
            }
            
            
            expressionFigure.setSurvivalInfo(titles,survivalInfoList, "META_GENE");
            
            expressionFigure.setFigureLineInfo(figureInfo);

            expressionFigure.savePNG("/Users/Scooter/Downloads/test.png");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
