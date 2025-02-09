package com.github.twohou.sonic;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class Channel {

    private Socket socket;

    private BufferedReader socketReader;

    private BufferedWriter socketWriter;

    private String password;

    /**
     * @param address           Address of Sonic server
     * @param port              Port of Sonic server
     * @param password          auth_password of Sonic server
     * @param connectionTimeout Connection timeout in milliseconds
     * @param readTimeout       Read timeout in milliseconds
     * @throws IOException
     */
    public Channel(
            String address,
            Integer port,
            String password,
            Integer connectionTimeout,
            Integer readTimeout
    ) throws IOException {

        Objects.requireNonNull(address);
        Objects.requireNonNull(password);
        Objects.requireNonNull(connectionTimeout);
        Objects.requireNonNull(readTimeout);

        this.password = password;

        this.socket = new Socket();
        //this.socket.connect(new InetSocketAddress(address, port), connectionTimeout);
        this.socket.connect(new InetSocketAddress(address, port), connectionTimeout);

        this.socket.setSoTimeout(readTimeout);
        this.socketReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.socketWriter = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));

        this.assertPrompt("^CONNECTED");
    }

    protected void send(String command) throws IOException {
        this.socketWriter.write(command + "\r\n");
        this.socketWriter.flush();
    }

    protected String readLine() throws IOException {
        return this.socketReader.readLine();
    }

    protected void assertOK() throws IOException {
        this.assertPrompt("^OK\r\n$");
    }

    protected Integer assertResult() throws IOException {
        String prompt = this.readLine();
        Pattern pattern = Pattern.compile("^RESULT ([0-9]+)$");
        Matcher matcher = pattern.matcher(prompt);
        if (!matcher.find()) {
            throw new SonicException("unexpected prompt: " + prompt);
        }
        return Integer.valueOf(matcher.group(1));
    }

    protected void assertPrompt(String regexp) throws IOException {
        String prompt = this.readLine();
        if (Pattern.matches(regexp, prompt)) {
            throw new SonicException("unexpected prompt: " + prompt);
        }
    }

    public void start(Mode mode) throws IOException {
        this.send(String.format("START %s %s", mode.name(), this.password));
        this.assertPrompt("^STARTED");
    }

    public void ping() throws IOException {
        this.send("PING");
        this.assertPrompt("^PONG\r\n$");
    }

    public void quit() throws IOException {
        this.send("QUIT");
        this.assertPrompt("^ENDED\r\n$");
        this.socket.close();
    }
}
