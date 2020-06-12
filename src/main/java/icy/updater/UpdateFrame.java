/**
 * 
 */
package icy.updater;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * @author stephane
 */
public class UpdateFrame extends JFrame
{
    /**
     * 
     */
    private static final long serialVersionUID = -1849973451683594479L;

    /**
     * gui
     */
    final JLabel title = new JLabel();
    final JProgressBar progress = new JProgressBar();
    final JTextPane infos = new JTextPane();
    final JButton closeBtn = new JButton("close");

    /**
     * @param title
     * @throws HeadlessException
     */
    public UpdateFrame(String title) throws HeadlessException
    {
        super(title);

        infos.setContentType("text/html");
        infos.setText("<html>");
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setDefaultLookAndFeelDecorated(true);

        setMinimumSize(new Dimension(640, 300));
        setPreferredSize(new Dimension(640, 300));
        setLocation(150, 150);

        build();
    }

    public void build()
    {
        setLayout(new BorderLayout());

        final JPanel topPanel = new JPanel();
        final JPanel mainPanel = new JPanel();
        final JPanel bottomPanel = new JPanel();

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        final JPanel labelPanel = new JPanel();
        final JPanel progressPanel = new JPanel();

        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS));
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.LINE_AXIS));

        title.setText("Waiting shutdown, please wait...");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setHorizontalTextPosition(SwingConstants.CENTER);

        infos.setEditable(false);
        infos.setMinimumSize(new Dimension(540, 160));
        infos.setPreferredSize(new Dimension(540, 160));

        progress.setMinimum(0);
        progress.setMaximum(100);

        closeBtn.setEnabled(false);
        closeBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // close frame
                UpdateFrame.this.dispose();
            }
        });

        labelPanel.add(Box.createHorizontalGlue());
        labelPanel.add(title);
        labelPanel.add(Box.createHorizontalGlue());

        progressPanel.add(Box.createHorizontalGlue());
        progressPanel.add(progress);
        progressPanel.add(Box.createHorizontalGlue());

        topPanel.add(labelPanel);
        topPanel.add(progressPanel);

        mainPanel.add(new JScrollPane(infos), BorderLayout.CENTER);

        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(closeBtn);
        bottomPanel.add(Box.createHorizontalGlue());

        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        validate();
        pack();
    }

    @Override
    public void setTitle(String text)
    {
        title.setText(text);
    }

    public void setCanClose(boolean value)
    {
        closeBtn.setEnabled(value);
    }

    public void addMessage(String message, boolean error)
    {
        SimpleAttributeSet set = new SimpleAttributeSet();

        if (error)
        {
            StyleConstants.setForeground(set, Color.red);
            // force frame visibility if error
            setVisible(true);
        }
        else
            StyleConstants.setForeground(set, Color.black);

        try
        {
            infos.getDocument().insertString(infos.getDocument().getLength(), message, set);
            infos.setCaretPosition(infos.getDocument().getLength());
        }
        catch (BadLocationException e)
        {
            // ignore
        }
    }

    public void setProgress(int value)
    {
        progress.setValue(value);
    }

    public void setProgressVisible(boolean value)
    {
        progress.setVisible(value);
    }
}
