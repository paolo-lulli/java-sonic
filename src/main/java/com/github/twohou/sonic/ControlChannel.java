package com.github.twohou.sonic;

import java.io.IOException;

public class ControlChannel extends Channel {
    public ControlChannel(String address, Integer port, String password,
                          Integer connectionTimeout, Integer readTimeout)
            throws IOException {
        super(address, port, password, connectionTimeout, readTimeout);
        this.start(Mode.control);
    }

    public void consolidate() throws IOException {
        this.send("TRIGGER consolidate");
        this.assertOK();
    }
}
