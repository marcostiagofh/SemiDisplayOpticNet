package projects.opticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class NewMessage extends Message {

    public NewMessage () { }

    @Override
    public Message clone () {
        return this;
    }


}
