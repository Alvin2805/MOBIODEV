import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.util.Scanner;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.fazecast.jSerialComm.SerialPort;

import javax.swing.JButton;

public class DataStream {

	private JFrame frame;
	private JTextField textField;
	static SerialPort chosenport;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DataStream window = new DataStream();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public DataStream() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1189, 690);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("MOBIODEV Data Streaming");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel.setBounds(28, 11, 258, 30);
		frame.getContentPane().add(lblNewLabel);
		
		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.setFont(new Font("Tahoma", Font.PLAIN, 15));
		comboBox.setBounds(28, 44, 122, 25);
		frame.getContentPane().add(comboBox);
		
		textField = new JTextField();
		textField.setText("115200");
		textField.setFont(new Font("Tahoma", Font.PLAIN, 15));
		textField.setBounds(160, 44, 65, 25);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton connect = new JButton("Connect");
		connect.setFont(new Font("Tahoma", Font.PLAIN, 15));
		connect.setBounds(235, 43, 141, 26);
		frame.getContentPane().add(connect);
		
		XYSeries dataxy = new XYSeries("Biosignal Stream");
		XYSeriesCollection dataset = new XYSeriesCollection(dataxy);
		JFreeChart datachart = ChartFactory.createXYLineChart("Data Series 1", "Samples (n)", "Amplitude (a.u.)", dataset);
		XYPlot xyplot = (XYPlot) datachart.getPlot();
		XYItemRenderer renderer = xyplot.getRenderer();
		renderer.setSeriesPaint(0, Color.red);
		NumberAxis domain1 = (NumberAxis) xyplot.getDomainAxis();
		domain1.setRange(0,1000);
		ChartPanel bingkai = new ChartPanel(datachart);
		bingkai.setBounds(28,80,1116,252);
		frame.getContentPane().add(bingkai);
		
		XYSeries dataxy2 = new XYSeries("Brain Data Stream");
		XYSeriesCollection dataset2 = new XYSeriesCollection(dataxy2);
		JFreeChart datachart2 = ChartFactory.createXYLineChart("Data Series 2", "Time (s)", "Power (a.u.)", dataset2);	
		ChartPanel bingkai2 = new ChartPanel(datachart2);
		XYPlot xyplot2 = (XYPlot) datachart2.getPlot();
		XYItemRenderer renderer2 = xyplot2.getRenderer();
		renderer2.setSeriesPaint(0, Color.blue);
		NumberAxis domain2 = (NumberAxis) xyplot2.getDomainAxis();
		domain2.setRange(0,100);
		bingkai2.setBounds(28,388,1116,252);
		frame.getContentPane().add(bingkai2);
		
		JComboBox<String> bandpower = new JComboBox<String>();
		bandpower.addItem("Delta");
		bandpower.addItem("Theta");
		bandpower.addItem("Alpha");
		bandpower.addItem("Beta");
		bandpower.addItem("Gamma");
		bandpower.setFont(new Font("Tahoma", Font.PLAIN, 15));
		bandpower.setBounds(28, 347, 122, 30);
		frame.getContentPane().add(bandpower);
		
		JLabel lblNewLabel_1 = new JLabel("Choose the brain wave (Delta,Theta, Alpha, Beta, Gamma)");
		lblNewLabel_1.setBounds(157, 347, 496, 30);
		frame.getContentPane().add(lblNewLabel_1);
		
		JButton ClearData = new JButton("Clear Records");
		ClearData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dataxy.clear();
				dataxy2.clear();
			}
		});
		ClearData.setFont(new Font("Tahoma", Font.PLAIN, 15));
		ClearData.setBounds(378, 43, 153, 26);
		frame.getContentPane().add(ClearData);
		
		SerialPort[] portnames = SerialPort.getCommPorts();
		int i;
		for(i = 0; i < portnames.length; i++) {
			comboBox.addItem(portnames[i].getSystemPortName());
		}
		
		connect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (connect.getText().equals("Connect")) {
					chosenport = SerialPort.getCommPort(comboBox.getSelectedItem().toString());
					chosenport.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
					chosenport.setBaudRate(Integer.parseInt(textField.getText()));
					if(chosenport.openPort()) {
						connect.setText("Disconnect");
						comboBox.setEnabled(false);
					}
					
					Thread thread = new Thread() {
						@Override
						public void run() {
							Scanner scanner = new Scanner(chosenport.getInputStream());
							int x = 0;
			
							
							try {
								
								FileWriter biodata = new FileWriter("biopotential_data.txt");
								FileWriter braindata = new FileWriter("brain_data.txt");
								
								while(scanner.hasNextLine()) {
									String line = scanner.nextLine();							
									
									try {
										
										if (line.length() > 8) {
											String[] temp = line.split(","); 
											System.out.println(line);
											braindata.write(line);
											braindata.write("\n");
											int totalpow = Integer.parseInt(temp[3]) +  Integer.parseInt(temp[4]) +  Integer.parseInt(temp[5]) +  Integer.parseInt(temp[6]) +  Integer.parseInt(temp[7]) +  Integer.parseInt(temp[8]) +  Integer.parseInt(temp[9]) +  Integer.parseInt(temp[10]);
											if (bandpower.getSelectedItem().equals("Delta")) {
												//System.out.println("Data Delta");
												if (x>10) {
													domain2.setRange(x-10,x);
													dataxy2.add(x++,(float) Integer.parseInt(temp[3])/(float)totalpow);
												}
												else {
													dataxy2.add(x++,(float) Integer.parseInt(temp[3])/(float)totalpow);
												}
												
											}
											else if (bandpower.getSelectedItem().equals("Theta")) {
												//System.out.println("Data Theta");
												if (x>10) {
													domain2.setRange(x-10,x);
													dataxy2.add(x++,(float)Integer.parseInt(temp[4])/(float)totalpow);
												}
												else {
													dataxy2.add(x++,(float)Integer.parseInt(temp[4])/(float)totalpow);
												}
												
											}
											else if (bandpower.getSelectedItem().equals("Alpha")) {
												//System.out.println("Data Alpha");
												if (x>10) {
													domain2.setRange(x-10,x);
													dataxy2.add(x++,(float)(Integer.parseInt(temp[5])+Integer.parseInt(temp[6]))/(float)totalpow);
												}
												else {
													dataxy2.add(x++,(float)(Integer.parseInt(temp[5])+Integer.parseInt(temp[6]))/(float)totalpow);
												}
												
											}
											else if (bandpower.getSelectedItem().equals("Beta")) {
												//System.out.println("Data Beta");
												if (x>10) {
													domain2.setRange(x-10,x);
													dataxy2.add(x++,(float)(Integer.parseInt(temp[7])+Integer.parseInt(temp[8]))/(float)totalpow);
												}
												else {
													dataxy2.add(x++,(float)(Integer.parseInt(temp[7])+Integer.parseInt(temp[8]))/(float)totalpow);
												}
												
											}
											else if (bandpower.getSelectedItem().equals("Gamma")) {
												//System.out.println("Data Gamma");
												if (x>10) {
													domain2.setRange(x-10,x);
													dataxy2.add(x++,(float)(Integer.parseInt(temp[9])+Integer.parseInt(temp[10]))/(float)totalpow);
												}
												else {
													dataxy2.add(x++,(float)(Integer.parseInt(temp[9])+Integer.parseInt(temp[10]))/(float)totalpow);
												}
											}
											
										}
										else if(line.length()<2){
											System.out.println("emptydata");
										}
										else{
											//System.out.println("Data ECG");
											String[] temp = line.split(",");
											System.out.println(temp[0]);
											if (x > 1000) {
												domain1.setRange(x-1000,x);
												dataxy.add(x++,Integer.parseInt(temp[0]));
											}
											else {
												dataxy.add(x++,Integer.parseInt(temp[0]));
											}
											
											biodata.write(temp[0]+"\n");
										}
										
									} catch (Exception e2) {
										// TODO: handle exception
										System.out.println("data error");
									}
								}
								
								braindata.close();
								biodata.close();
							} catch (Exception e2) {
								// TODO: handle exception
							}
							scanner.close();
						}
					};
					thread.start();
				}
				else {
					chosenport.closePort();
					comboBox.setEnabled(true);
					connect.setText("Connect");
					JOptionPane.showConfirmDialog(null, "Do you want to save the records ?");
				}
				
			}
		});
	}
}
