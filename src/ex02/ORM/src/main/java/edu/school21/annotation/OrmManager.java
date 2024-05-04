package edu.school21.annotation;

import com.google.auto.service.AutoService;
import edu.school21.DatabaseManager.DatabaseManager;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;


@SupportedAnnotationTypes("edu.school21.annotation.OrmEntity")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(OrmManager.class)
public class OrmManager extends AbstractProcessor {
    Connection connection;
    public OrmManager() { this.connection = null; }
    public OrmManager(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("Processing annotations...");
        ArrayList<?>[] entry = parseAnnotations(roundEnv);
        return createTable(roundEnv, entry);
    }

    private boolean createTable(RoundEnvironment roundEnv, ArrayList<?>[] Info) {

        try {
            System.out.println("Executing SQL-script...");
            for (Element element : roundEnv.getElementsAnnotatedWith(OrmEntity.class)) {

                if (connection == null) connection = DatabaseManager.getConnection();
                System.out.println("2. Connection: " + connection);

                Statement statement = connection.createStatement();
                OrmEntity ormColumnAnnotation = element.getAnnotation(OrmEntity.class);
                String query = String.format("DROP TABLE IF EXISTS %s CASCADE;", ormColumnAnnotation.table());

                System.out.println("------- SQL-script -------");
                System.out.println(query);
                System.out.println("--------------------------");
                System.out.println("Executing...");
                try {
                    statement.execute(query);
                } catch (SQLException e) {
                    throw new RuntimeException("Fatal error while executing script:\n " + e.getMessage());
                }
                System.out.println("Finished...");

                StringBuilder createTableQuery = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                        .append(ormColumnAnnotation.table())
                        .append(" (");
                ArrayList<String> columnNames = (ArrayList<String>) Info[0];
                ArrayList<Boolean> flags = (ArrayList<Boolean>) Info[1];
                if (flags.get(2)) {
                    createTableQuery.append("id INT AUTO_INCREMENT PRIMARY KEY, ");
                }

                for (int i = 0; i < columnNames.size(); i++) {
                    if (i > 0) {
                        createTableQuery.append(", ");
                    }
                    createTableQuery.append(columnNames.get(i)).append(" VARCHAR(100)");
                    if (!flags.get(1)) createTableQuery.append(" NOT NULL");
                    if (flags.get(0)) createTableQuery.append(" UNIQUE");
                }
                createTableQuery.append(");");

                System.out.println("------- SQL-script -------");
                System.out.println(createTableQuery);
                System.out.println("--------------------------");
                System.out.println("Executing...");

                try {
                    statement.execute(createTableQuery.toString());
                } catch (SQLException e) {
                    throw new RuntimeException("Fatal error while executing script:\n " + e.getMessage());
                }

                System.out.println("Finished...");

                System.out.println("-------- col Names -------");
                columnNames.forEach(System.out::println);
                System.out.println("---------- Flags ---------");
                flags.forEach(System.out::println);
                System.out.println("--------------------------");
            }
            return true;
        } catch (RuntimeException | SQLException e) {
            throw new RuntimeException("Fatal error while creating table:\n" + e.getMessage());
        }
    }

    public ArrayList<?>[] parseAnnotations(RoundEnvironment roundEnv) {
        List<Object> colNames = roundEnv.getElementsAnnotatedWith(OrmEntity.class).stream()
                .flatMap(element -> element.getEnclosedElements().stream())
                .map(element1 -> {
                    OrmColumn ormColumnAnnotation = element1.getAnnotation(OrmColumn.class);
                    OrmColumnId ormColumnIdAnnotation = element1.getAnnotation(OrmColumnId.class);
                    return ormColumnAnnotation != null ? ormColumnAnnotation.name() : ormColumnIdAnnotation != null ?
                            Arrays.asList(
                                    ormColumnIdAnnotation.constraints().unique(),
                                    ormColumnIdAnnotation.constraints().allowNull(),
                                    ormColumnIdAnnotation.constraints().primaryKey()
                            ) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        ArrayList<String> columnNames = colNames.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Boolean> flags = colNames.stream()
                .filter(List.class::isInstance)
                .flatMap(list -> ((List<Boolean>) list).stream())
                .collect(Collectors.toCollection(ArrayList::new));

        return new ArrayList<?>[]{columnNames, flags};
    }

    public void save(Object entity) {
        Class<?> tmp = entity.getClass();
        OrmEntity ormEntityAnnotation = tmp.getAnnotation(OrmEntity.class);
        if (ormEntityAnnotation == null) {
            System.err.println("Entity is not annotated!");
            return;
        }
        String tableName = ormEntityAnnotation.table();
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder valuesBuilder = new StringBuilder(") VALUES (");
        Field[] fields = tmp.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isAnnotationPresent(OrmColumn.class)) {
                OrmColumn column = fields[i].getAnnotation(OrmColumn.class);
                queryBuilder.append(column.name());
                valuesBuilder.append("?");
                if (i < fields.length - 1) {
                    queryBuilder.append(", ");
                    valuesBuilder.append(", ");
                }
            }
        }
        String query = queryBuilder.append(valuesBuilder).append(")").toString();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            int parameterIndex = 1;
            for (Field field : fields) {
                if (field.isAnnotationPresent(OrmColumn.class)) {
                    field.setAccessible(true);
                    statement.setObject(parameterIndex++, field.get(entity));
                    field.setAccessible(false);
                }
            }
            statement.executeUpdate();
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException("Fatal error while creating table:\n" + e.getMessage());
        }
        System.out.println(query);
    }

    public void update(Object entity) {
        Class<?> tmp = entity.getClass();
        OrmEntity ormEntityAnnotation = tmp.getAnnotation(OrmEntity.class);
        if (ormEntityAnnotation == null) {
            System.err.println("Entity is not annotated!");
            return;
        }
        String tableName = ormEntityAnnotation.table();
        StringBuilder queryBuilder = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        Field[] fields = tmp.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(OrmColumn.class)) {
                OrmColumn column = field.getAnnotation(OrmColumn.class);
                queryBuilder.append(column.name()).append(" = ?, ");
            }
        }
        queryBuilder.setLength(queryBuilder.length() - 2);
        queryBuilder.append(" WHERE ");
        Field idField = fields[0];
        queryBuilder.append("id").append(" = ?");
        String query = queryBuilder.toString();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            int parameterIndex = 1;
            for (Field field : fields) {
                if (field.isAnnotationPresent(OrmColumn.class)) {
                    field.setAccessible(true);
                    statement.setObject(parameterIndex++, field.get(entity));
                    field.setAccessible(false);
                }
            }
            statement.setObject(parameterIndex, idField.get(entity));
            statement.executeUpdate();
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException("Fatal error while creating table:\n" + e.getMessage());
        }
        System.out.println(query);
    }

    public <T> T findById(Long id, Class<T> aClass) {
        if (!aClass.isAnnotationPresent(OrmEntity.class)) {
            return null;
        }

        OrmEntity ormEntityAnnotation = aClass.getAnnotation(OrmEntity.class);
        String tableName = ormEntityAnnotation.table();

        Field[] fields = aClass.getDeclaredFields();
        String queryBuilder = "SELECT * FROM " +
                tableName +
                " WHERE id = ?";

        T object = null;

        try (PreparedStatement statement = connection.prepareStatement(queryBuilder)) {

            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                System.err.println("No such entity!");
                return null;
            }

            object = aClass.newInstance();

            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(OrmColumnId.class)) {
                    field.set(object, resultSet.getInt(1));
                } else if (field.isAnnotationPresent(OrmColumn.class)) {
                    OrmColumn ormColumnAnnotation = field.getAnnotation(OrmColumn.class);
                    String columnName = ormColumnAnnotation.name();
                    field.set(object, resultSet.getObject(columnName));
                }
            }
        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            System.out.println("Fatal error: " + e.getMessage());
        }
        return object;
    }
}

