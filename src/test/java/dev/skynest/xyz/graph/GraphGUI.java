package dev.skynest.xyz.graph;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GraphGUI extends JFrame {

    public GraphGUI(List<Long> saveTimes, List<Long> removeTimes) {
        setTitle("Grafico dei Tempi di Esecuzione");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        XYSeries saveSeries = new XYSeries("Save Times");
        XYSeries removeSeries = new XYSeries("Remove Times");

        for (int i = 0; i < saveTimes.size(); i++) {
            saveSeries.add(i + 1, saveTimes.get(i));
            removeSeries.add(i + 1, removeTimes.get(i));
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(saveSeries);
        dataset.addSeries(removeSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Grafico dei Tempi di Esecuzione",
                "Numero di Operazioni",
                "Tempo (ms)",
                dataset
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);
    }
}

