/*
 * Copyright (C) 2017 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package retrofit2;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;
import javax.annotation.Nullable;
import okhttp3.ResponseBody;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import retrofit2.http.GET;

import static org.assertj.core.api.Assertions.assertThat;

public final class OptionalConverterFactoryTest {
  interface Service {
    @GET("/") Call<Optional<Object>> optional();
    @GET("/") Call<Object> object();
  }

  @Rule public final MockWebServer server = new MockWebServer();

  private Service service;

  @Before public void setUp() {
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(server.url("/"))
        .addConverterFactory(new ObjectToNullConverterFactory())
        .build();
    service = retrofit.create(Service.class);
  }

  @Test public void optional() throws IOException {
    server.enqueue(new MockResponse());

    Optional<Object> optional = service.optional().execute().body();
    assertThat(optional).isNotNull();
    assertThat(optional.isPresent()).isFalse();
  }

  @Test public void onlyMatchesOptional() throws IOException {
    server.enqueue(new MockResponse());

    Object body = service.object().execute().body();
    assertThat(body).isNull();
  }

  static final class ObjectToNullConverterFactory extends Converter.Factory {
    @Override public @Nullable Converter<ResponseBody, ?> responseBodyConverter(
        Type type, Annotation[] annotations, Retrofit retrofit) {
      if (type != Object.class) {
        return null;
      }
      return new Converter<ResponseBody, Object>() {
        @Override public Object convert(ResponseBody value) {
          return null;
        }
      };
    }
  }
}
