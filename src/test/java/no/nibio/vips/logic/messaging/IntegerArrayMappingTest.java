package no.nibio.vips.logic.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.persistence.Column;
import java.lang.reflect.AnnotatedElement;
import java.sql.Types;
import java.util.List;
import no.nibio.vips.logic.entity.CropCategory;
import no.nibio.vips.logic.entity.CropPest;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.util.PostgresListArrayType;
import org.hibernate.annotations.Type;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.dialect.Dialect;
import org.hibernate.tool.schema.extract.spi.ColumnInformation;
import org.junit.jupiter.api.Test;

class IntegerArrayMappingTest {

    @Test
    void subscriptionArrayColumnsHaveResolvableSqlTypes() throws Exception {
        var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                .applySetting("hibernate.boot.allow_jdbc_metadata_access", false)
                .build();
        try {
            var metadata = new MetadataSources(registry)
                    .addAnnotatedClass(ForecastEventNotificationSubscription.class)
                    .addAnnotatedClass(MessageNotificationSubscription.class)
                    .addAnnotatedClass(ObservationNotificationSubscription.class)
                    .buildMetadata();
            assertEquals("integer[]", metadata.getEntityBinding(ForecastEventNotificationSubscription.class.getName())
                    .getProperty("weatherStationIds").getColumns().get(0).getSqlType(metadata));
            assertEquals("integer[]", metadata.getEntityBinding(ForecastEventNotificationSubscription.class.getName())
                    .getProperty("cropCategoryIds").getColumns().get(0).getSqlType(metadata));
            assertEquals("integer[]", metadata.getEntityBinding(MessageNotificationSubscription.class.getName())
                    .getProperty("messageTagIds").getColumns().get(0).getSqlType(metadata));
            assertEquals("integer[]", metadata.getEntityBinding(MessageNotificationSubscription.class.getName())
                    .getProperty("cropCategoryIds").getColumns().get(0).getSqlType(metadata));
            assertEquals("integer[]", metadata.getEntityBinding(ObservationNotificationSubscription.class.getName())
                    .getProperty("cropCategoryIds").getColumns().get(0).getSqlType(metadata));
            var column = metadata.getEntityBinding(ForecastEventNotificationSubscription.class.getName())
                    .getProperty("cropCategoryIds").getColumns().get(0);
            var databaseColumn = mock(ColumnInformation.class);
            when(databaseColumn.getTypeCode()).thenReturn(Types.ARRAY);
            when(databaseColumn.getTypeName()).thenReturn("_int4");
            var hasMatchingType = Class.forName("org.hibernate.tool.schema.internal.ColumnDefinitions")
                    .getDeclaredMethod("hasMatchingType", org.hibernate.mapping.Column.class, ColumnInformation.class, Metadata.class, Dialect.class);
            hasMatchingType.setAccessible(true);
            assertTrue((boolean) hasMatchingType.invoke(null, column, databaseColumn, metadata,
                    metadata.getDatabase().getDialect()));
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    void allArrayMappingsDeclareTheirPostgresType() throws Exception {
        var mappings = List.<AnnotatedElement>of(
                ForecastEventNotificationSubscription.class.getDeclaredField("weatherStationIds"),
                ForecastEventNotificationSubscription.class.getDeclaredField("cropCategoryIds"),
                MessageNotificationSubscription.class.getDeclaredField("messageTagIds"),
                MessageNotificationSubscription.class.getDeclaredField("cropCategoryIds"),
                ObservationNotificationSubscription.class.getDeclaredField("cropCategoryIds"),
                CropCategory.class.getDeclaredField("cropOrganismIds"),
                CropPest.class.getDeclaredField("pestOrganismIds"),
                ForecastConfiguration.class.getDeclaredField("gridWeatherStationPointOfInterestIds"),
                Message.class.getDeclaredMethod("getCropCategoryIds"));
        for (var mapping : mappings) {
            assertEquals(PostgresListArrayType.class, mapping.getAnnotation(Type.class).value());
            assertEquals("integer[]", mapping.getAnnotation(Column.class).columnDefinition());
        }
    }
}
