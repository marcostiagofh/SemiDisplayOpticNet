package projects.opticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class AllowRoutingMessage extends Message {

    public AllowRoutingMessage () { }
    
    @Override
    public Message clone () {
        return this;
    }


}
