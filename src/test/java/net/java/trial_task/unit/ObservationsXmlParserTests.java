package net.java.trial_task.unit;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import net.java.trial_task.weather.exceptions.WeatherParseException;
import net.java.trial_task.weather.service.ObservationsXmlParser;

@SpringBootTest 
public class ObservationsXmlParserTests {
  private final ObservationsXmlParser parser = new ObservationsXmlParser();

  private static final String FULL_XML = """
    <?xml version="1.0" encoding="UTF-8"?>
    <observations timestamp="1760000000">
      <station>
        <name>Tartu-Tõravere</name>
        <wmocode>26242</wmocode>
        <phenomenon>Light snow shower</phenomenon>
        <airtemperature>-2.1</airtemperature>
        <windspeed>4.7</windspeed>
        <unknowntag>ignored</unknowntag>
      </station>
      <station>
        <name>Pärnu</name>
        <wmocode>41803</wmocode>
        <phenomenon/>
        <airtemperature/>
        <windspeed>1.0</windspeed>
      </station>
    </observations>
    """;

  @Test
  void parsesTimestampAndStations() {
    var dto = parser.parse(FULL_XML.getBytes(StandardCharsets.UTF_8));

    assertThat(dto.timestamp()).isEqualTo(1760000000L);
    assertThat(dto.stations()).hasSize(2);
    var tartu = dto.stations().get(0);
    assertThat(tartu.name()).isEqualTo("Tartu-Tõravere");
    assertThat(tartu.wmoCode()).isEqualTo("26242");
    assertThat(tartu.phenomenon()).isEqualTo("Light snow shower");
    assertThat(tartu.airTemperature()).isEqualTo(-2.1);
    assertThat(tartu.windSpeed()).isEqualTo(4.7);
  }

  @Test
  void emptyElementsBecomeNull() {
    var dto = parser.parse(FULL_XML.getBytes(StandardCharsets.UTF_8));

    var parnu = dto.stations().get(1);
    assertThat(parnu.phenomenon()).isNull();
    assertThat(parnu.airTemperature()).isNull();
    assertThat(parnu.windSpeed()).isEqualTo(1.0);
  }

  @Test
  void malformedXmlThrowsParseException() {
    byte[] broken = "<observations><station>".getBytes(StandardCharsets.UTF_8);

    assertThatThrownBy(() -> parser.parse(broken))
      .isInstanceOf(WeatherParseException.class);
  }

  @Test
  void nonNumericTemperatureThrowsParseException() {
    String xml = """
      <?xml version="1.0" encoding="UTF-8"?>
      <observations timestamp="1">
        <station><name>X</name><wmocode>1</wmocode><airtemperature>abc</airtemperature></station>
      </observations>
      """;

    assertThatThrownBy(() -> parser.parse(xml.getBytes(StandardCharsets.UTF_8)))
      .isInstanceOf(WeatherParseException.class);
  }
}
