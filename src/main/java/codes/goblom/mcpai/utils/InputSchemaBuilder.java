/*
 * The MIT License
 *
 * Copyright 2026 Bryan.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package codes.goblom.mcpai.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;

/**
 *
 * @author Bryan
 */
public class InputSchemaBuilder {
    
    public static InputSchemaBuilder newBuilder() {
        return new InputSchemaBuilder();
    }
    
    public static InputSchemaBuilder newBuilder(ParameterType type) {
        return new InputSchemaBuilder(type);
    }
    
    public enum ParameterType {
        String("string"),
        Number("number"),
        Object("object"),
        Array("array"),
        Boolean("boolean"),
        Null("null");
        
        final String type;
        
        ParameterType(String str) {
            this.type = str;
        }
        
        @Override
        public String toString() {
            return this.type;
        }
    }
    
    private final Map<String, Object> schema = Maps.newHashMap();
    private final Map<String, Object> properties = Maps.newHashMap();
    private final List<String> required = Lists.newArrayList();
    
    private InputSchemaBuilder() {
        this(ParameterType.Object);
    }
    
    private InputSchemaBuilder(ParameterType type) {
        type(type);
    }
    
    public InputSchemaBuilder type(ParameterType type) {
        this.schema.put("type", type.toString());
        
        return this;
    }
    
    public InputSchemaBuilder additionalProperties(boolean additionalProperties) {
        this.schema.put("additionalProperties", additionalProperties);
        
        return this;
    }
    
    public InputSchemaBuilder addProperty(boolean required, String name, ParameterType type) {
        return addProperty(required, name, type, null);
    }
    
    public InputSchemaBuilder addProperty(boolean required, String name, ParameterType type, String description) {
        Map<String, String> propertyMap = Maps.newHashMap();
        
        propertyMap.put("type", type.toString());
        if (description != null && !description.isEmpty()) {
            propertyMap.put("description", description);
        }
        
        this.properties.put(name, propertyMap);
        
        if (required) {
            this.required.add(name);
        }
        
        return this;
    }
    
    public InputSchemaBuilder addRequiredProperty(String name, ParameterType type) {
        return addRequiredProperty(name, type, null);
    }
    
    public InputSchemaBuilder addRequiredProperty(String name, ParameterType type, String description) {
        return addProperty(true, name, type, description);
    }
    
    public InputSchemaBuilder addOptionalProperty(String name, ParameterType type) {
        return addOptionalProperty(name, type, null);
    }
    
    public InputSchemaBuilder addOptionalProperty(String name, ParameterType type, String description) {
        return addProperty(false, name, type, description);
    }
    
    private Map<String, Object> build() {
        this.schema.put("properties", this.properties);
        
        if (!this.required.isEmpty()) {
            this.schema.put("required", this.required);
        }
        
        return schema;
    }
    
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            return mapper.writeValueAsString(build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
