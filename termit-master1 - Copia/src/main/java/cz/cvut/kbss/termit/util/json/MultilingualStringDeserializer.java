package cz.cvut.kbss.termit.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import cz.cvut.kbss.jopa.model.MultilingualString;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class MultilingualStringDeserializer extends StdDeserializer<MultilingualString> {

    public MultilingualStringDeserializer() {
        super(MultilingualString.class);
    }

    JsonParser jsonParser;
    final MultilingualString result = new MultilingualString();
    
  


	@Override
	public MultilingualString deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		// TODO Auto-generated method stub
		return null;
	}
}
