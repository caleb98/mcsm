package ccode.mcsm;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;

import com.esotericsoftware.jsonbeans.JsonException;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Excluder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import ccode.mcsm.backup.BackupPolicy;
import ccode.mcsm.backup.MaxCapacityPolicy;
import ccode.mcsm.backup.MaxCountPolicy;
import ccode.mcsm.backup.NoLimitPolicy;

public class Json {

	private static final Gson gson;
	
	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(BackupPolicy.class, new BackupPolicySerializer());
		gson = gsonBuilder.create();
	}
	
	private static class BackupPolicySerializer implements JsonSerializer<BackupPolicy>, JsonDeserializer<BackupPolicy> {
		@Override
		public JsonElement serialize(BackupPolicy src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject policy = new JsonObject();
			if(src instanceof NoLimitPolicy) {
				policy.addProperty("type", NoLimitPolicy.class.getSimpleName());
			}
			else if(src instanceof MaxCountPolicy) {
				MaxCountPolicy maxCountPolicy = (MaxCountPolicy) src;
				
				policy.addProperty("type", MaxCountPolicy.class.getSimpleName());
				policy.addProperty("maxBackups", maxCountPolicy.getMaxBackups());
			}
			else if(src instanceof MaxCapacityPolicy) {
				MaxCapacityPolicy maxCapacityPolicy = (MaxCapacityPolicy) src;
				
				policy.addProperty("type", MaxCapacityPolicy.class.getSimpleName());
				policy.addProperty("maxBytes", maxCapacityPolicy.getMaxBytes());
			}
			else {
				throw new JsonException("Unable to serialize BackupPolicy of type " + src.getClass());
			}
			return policy;
		}

		@Override
		public BackupPolicy deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			
			JsonObject policyJson = json.getAsJsonObject();
			BackupPolicy policy;
			
			if(!policyJson.has("type")) {
				throw new JsonParseException("No type listed for BackupPolicy.");
			}
			
			String type = policyJson.get("type").getAsString();
			
			if(type.equals(NoLimitPolicy.class.getSimpleName())) {
				policy = new NoLimitPolicy(null);
			}
			else if(type.equals(MaxCountPolicy.class.getSimpleName())) {
				policy = new MaxCountPolicy(null, policyJson.get("maxBackups").getAsInt());
			}
			else if(type.equals(MaxCapacityPolicy.class.getSimpleName())) {
				policy = new MaxCapacityPolicy(null, policyJson.get("maxBytes").getAsLong());
			}
			else {
				throw new JsonException("Invalid backup policy type.");
			}
			
			return policy;
		}
	}

	/**
	 * @return
	 * @see com.google.gson.Gson#newBuilder()
	 */
	public static GsonBuilder newBuilder() {
		return gson.newBuilder();
	}

	/**
	 * @return
	 * @see com.google.gson.Gson#excluder()
	 */
	public static Excluder excluder() {
		return gson.excluder();
	}

	/**
	 * @return
	 * @see com.google.gson.Gson#fieldNamingStrategy()
	 */
	public static FieldNamingStrategy fieldNamingStrategy() {
		return gson.fieldNamingStrategy();
	}

	/**
	 * @return
	 * @see com.google.gson.Gson#serializeNulls()
	 */
	public static boolean serializeNulls() {
		return gson.serializeNulls();
	}

	/**
	 * @return
	 * @see com.google.gson.Gson#htmlSafe()
	 */
	public static boolean htmlSafe() {
		return gson.htmlSafe();
	}

	/**
	 * @param <T>
	 * @param type
	 * @return
	 * @see com.google.gson.Gson#getAdapter(com.google.gson.reflect.TypeToken)
	 */
	public static <T> TypeAdapter<T> getAdapter(TypeToken<T> type) {
		return gson.getAdapter(type);
	}

	/**
	 * @param <T>
	 * @param skipPast
	 * @param type
	 * @return
	 * @see com.google.gson.Gson#getDelegateAdapter(com.google.gson.TypeAdapterFactory,
	 *      com.google.gson.reflect.TypeToken)
	 */
	public static <T> TypeAdapter<T> getDelegateAdapter(TypeAdapterFactory skipPast, TypeToken<T> type) {
		return gson.getDelegateAdapter(skipPast, type);
	}

	/**
	 * @param <T>
	 * @param type
	 * @return
	 * @see com.google.gson.Gson#getAdapter(java.lang.Class)
	 */
	public static <T> TypeAdapter<T> getAdapter(Class<T> type) {
		return gson.getAdapter(type);
	}

	/**
	 * @param src
	 * @return
	 * @see com.google.gson.Gson#toJsonTree(java.lang.Object)
	 */
	public static JsonElement toJsonTree(Object src) {
		return gson.toJsonTree(src);
	}

	/**
	 * @param src
	 * @param typeOfSrc
	 * @return
	 * @see com.google.gson.Gson#toJsonTree(java.lang.Object,
	 *      java.lang.reflect.Type)
	 */
	public static JsonElement toJsonTree(Object src, Type typeOfSrc) {
		return gson.toJsonTree(src, typeOfSrc);
	}

	/**
	 * @param src
	 * @return
	 * @see com.google.gson.Gson#toJson(java.lang.Object)
	 */
	public static String toJson(Object src) {
		return gson.toJson(src);
	}

	/**
	 * @param src
	 * @param typeOfSrc
	 * @return
	 * @see com.google.gson.Gson#toJson(java.lang.Object, java.lang.reflect.Type)
	 */
	public static String toJson(Object src, Type typeOfSrc) {
		return gson.toJson(src, typeOfSrc);
	}

	/**
	 * @param src
	 * @param writer
	 * @throws JsonIOException
	 * @see com.google.gson.Gson#toJson(java.lang.Object, java.lang.Appendable)
	 */
	public static void toJson(Object src, Appendable writer) throws JsonIOException {
		gson.toJson(src, writer);
	}

	/**
	 * @param src
	 * @param typeOfSrc
	 * @param writer
	 * @throws JsonIOException
	 * @see com.google.gson.Gson#toJson(java.lang.Object, java.lang.reflect.Type,
	 *      java.lang.Appendable)
	 */
	public static void toJson(Object src, Type typeOfSrc, Appendable writer) throws JsonIOException {
		gson.toJson(src, typeOfSrc, writer);
	}

	/**
	 * @param src
	 * @param typeOfSrc
	 * @param writer
	 * @throws JsonIOException
	 * @see com.google.gson.Gson#toJson(java.lang.Object, java.lang.reflect.Type,
	 *      com.google.gson.stream.JsonWriter)
	 */
	public static void toJson(Object src, Type typeOfSrc, JsonWriter writer) throws JsonIOException {
		gson.toJson(src, typeOfSrc, writer);
	}

	/**
	 * @param jsonElement
	 * @return
	 * @see com.google.gson.Gson#toJson(com.google.gson.JsonElement)
	 */
	public static String toJson(JsonElement jsonElement) {
		return gson.toJson(jsonElement);
	}

	/**
	 * @param jsonElement
	 * @param writer
	 * @throws JsonIOException
	 * @see com.google.gson.Gson#toJson(com.google.gson.JsonElement,
	 *      java.lang.Appendable)
	 */
	public static void toJson(JsonElement jsonElement, Appendable writer) throws JsonIOException {
		gson.toJson(jsonElement, writer);
	}

	/**
	 * @param writer
	 * @return
	 * @throws IOException
	 * @see com.google.gson.Gson#newJsonWriter(java.io.Writer)
	 */
	public static JsonWriter newJsonWriter(Writer writer) throws IOException {
		return gson.newJsonWriter(writer);
	}

	/**
	 * @param reader
	 * @return
	 * @see com.google.gson.Gson#newJsonReader(java.io.Reader)
	 */
	public static JsonReader newJsonReader(Reader reader) {
		return gson.newJsonReader(reader);
	}

	/**
	 * @param jsonElement
	 * @param writer
	 * @throws JsonIOException
	 * @see com.google.gson.Gson#toJson(com.google.gson.JsonElement,
	 *      com.google.gson.stream.JsonWriter)
	 */
	public static void toJson(JsonElement jsonElement, JsonWriter writer) throws JsonIOException {
		gson.toJson(jsonElement, writer);
	}

	/**
	 * @param <T>
	 * @param json
	 * @param classOfT
	 * @return
	 * @throws JsonSyntaxException
	 * @see com.google.gson.Gson#fromJson(java.lang.String, java.lang.Class)
	 */
	public static <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
		return gson.fromJson(json, classOfT);
	}

	/**
	 * @param <T>
	 * @param json
	 * @param typeOfT
	 * @return
	 * @throws JsonSyntaxException
	 * @see com.google.gson.Gson#fromJson(java.lang.String, java.lang.reflect.Type)
	 */
	public static <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
		return gson.fromJson(json, typeOfT);
	}

	/**
	 * @param <T>
	 * @param json
	 * @param classOfT
	 * @return
	 * @throws JsonSyntaxException
	 * @throws JsonIOException
	 * @see com.google.gson.Gson#fromJson(java.io.Reader, java.lang.Class)
	 */
	public static <T> T fromJson(Reader json, Class<T> classOfT) throws JsonSyntaxException, JsonIOException {
		return gson.fromJson(json, classOfT);
	}

	/**
	 * @param <T>
	 * @param json
	 * @param typeOfT
	 * @return
	 * @throws JsonIOException
	 * @throws JsonSyntaxException
	 * @see com.google.gson.Gson#fromJson(java.io.Reader, java.lang.reflect.Type)
	 */
	public static <T> T fromJson(Reader json, Type typeOfT) throws JsonIOException, JsonSyntaxException {
		return gson.fromJson(json, typeOfT);
	}

	/**
	 * @param <T>
	 * @param reader
	 * @param typeOfT
	 * @return
	 * @throws JsonIOException
	 * @throws JsonSyntaxException
	 * @see com.google.gson.Gson#fromJson(com.google.gson.stream.JsonReader,
	 *      java.lang.reflect.Type)
	 */
	public static <T> T fromJson(JsonReader reader, Type typeOfT) throws JsonIOException, JsonSyntaxException {
		return gson.fromJson(reader, typeOfT);
	}

	/**
	 * @param <T>
	 * @param json
	 * @param classOfT
	 * @return
	 * @throws JsonSyntaxException
	 * @see com.google.gson.Gson#fromJson(com.google.gson.JsonElement,
	 *      java.lang.Class)
	 */
	public static <T> T fromJson(JsonElement json, Class<T> classOfT) throws JsonSyntaxException {
		return gson.fromJson(json, classOfT);
	}

	/**
	 * @param <T>
	 * @param json
	 * @param typeOfT
	 * @return
	 * @throws JsonSyntaxException
	 * @see com.google.gson.Gson#fromJson(com.google.gson.JsonElement,
	 *      java.lang.reflect.Type)
	 */
	public static <T> T fromJson(JsonElement json, Type typeOfT) throws JsonSyntaxException {
		return gson.fromJson(json, typeOfT);
	}

}
