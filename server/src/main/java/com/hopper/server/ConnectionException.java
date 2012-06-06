/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package com.hopper.server;
@SuppressWarnings("all")
public class ConnectionException extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ConnectionException\",\"namespace\":\"com.hopper.server\",\"fields\":[]}");
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /** Creates a new ConnectionException RecordBuilder */
  public static com.hopper.server.ConnectionException.Builder newBuilder() {
    return new com.hopper.server.ConnectionException.Builder();
  }
  
  /** Creates a new ConnectionException RecordBuilder by copying an existing Builder */
  public static com.hopper.server.ConnectionException.Builder newBuilder(com.hopper.server.ConnectionException.Builder other) {
    return new com.hopper.server.ConnectionException.Builder(other);
  }
  
  /** Creates a new ConnectionException RecordBuilder by copying an existing ConnectionException instance */
  public static com.hopper.server.ConnectionException.Builder newBuilder(com.hopper.server.ConnectionException other) {
    return new com.hopper.server.ConnectionException.Builder(other);
  }
  
  /**
   * RecordBuilder for ConnectionException instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<ConnectionException>
    implements org.apache.avro.data.RecordBuilder<ConnectionException> {


    /** Creates a new Builder */
    private Builder() {
      super(com.hopper.server.ConnectionException.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(com.hopper.server.ConnectionException.Builder other) {
      super(other);
    }
    
    /** Creates a Builder by copying an existing ConnectionException instance */
    private Builder(com.hopper.server.ConnectionException other) {
            super(com.hopper.server.ConnectionException.SCHEMA$);
    }

    @Override
    public ConnectionException build() {
      try {
        ConnectionException record = new ConnectionException();
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}