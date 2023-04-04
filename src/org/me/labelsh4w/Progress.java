/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.me.labelsh4w;

import javax.swing.JProgressBar;

/**
 *
 * @author tiago
 */
public class Progress implements Runnable {

    private static final int DELAY = 10;

    JProgressBar progressBar;

    public Progress(JProgressBar bar) {
        progressBar = bar;
    }

    @Override
    public void run() {
        int minimum = progressBar.getMinimum();
        int maximum = progressBar.getMaximum();
        for (int i = minimum; i < maximum; i++) {
            try {
                int value = progressBar.getValue();
                progressBar.setValue(value + 1);
                Thread.sleep(DELAY);
            } catch (InterruptedException ignoredException) {
            }
        }
    }
}
