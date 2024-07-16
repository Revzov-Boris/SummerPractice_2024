import java.sql.*;
import java.util.Scanner;

public class JDBCRunner {

    private static final String PROTOCOL = "jdbc:postgresql://";        // URL-prefix
    private static final String DRIVER = "org.postgresql.Driver";       // Driver name
    private static final String URL_LOCALE_NAME = "localhost/";         // ваш компьютер + порт по умолчанию

    private static final String DATABASE_NAME = "Schools";          // FIXME имя базы

    public static final String DATABASE_URL = PROTOCOL + URL_LOCALE_NAME + DATABASE_NAME;
    public static final String USER_NAME = "postgres";                  // FIXME имя пользователя
    public static final String DATABASE_PASS = "postgres";              // FIXME пароль базы данных

    public static String spaceN(int n) {
        return " ".repeat(n);
    }

    public static void showPupils(int type) throws SQLException{
        Connection connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS);
        Statement statement = connection.createStatement();
        String sqlRequest;
        if (type == 1) { // вывести всех
            sqlRequest = "SELECT pupils.* FROM pupils;";
        } else { // Вывести учеников, чей учитель имеет стаж 5 лет и менее
            sqlRequest = "SELECT pupils.*, teachers.name AS \"учитель\", teachers.old, teachers.experience, teachers.education FROM pupils JOIN teachers ON pupils.teacher_id = teachers.id WHERE experience <= 5 ORDER BY experience DESC;";
        }
        ResultSet resultSet = statement.executeQuery(sqlRequest);
        System.out.println("id" + spaceN(3) + " | " + "teacher_id" + " | "  + "name" + spaceN(28) + " | " + "old" + " | " + "gender" + " | " + "title_class" + " | " + "avr_rating");
        System.out.println("--------------------------------------------------------------------------------------------------");
        while (resultSet.next()) {
            String id = resultSet.getString("id");
            String teacher_id = resultSet.getString("teacher_id");
            String name = resultSet.getString("name");
            String old = resultSet.getString("old");
            String gender = resultSet.getString("gender");
            String title_class = resultSet.getString("title_class");
            String avr_rating = resultSet.getString("avr_rating");
            System.out.print(id + spaceN(5 - id.length()) + " | ");
            System.out.print(teacher_id + spaceN(10 - teacher_id.length()) + " | ");
            System.out.print(name + spaceN(32 - name.length()) + " | ");
            System.out.print(old + spaceN(3 - old.length()) + " | ");
            System.out.print(gender + spaceN(5) + " | ");
            System.out.print(title_class+ spaceN(11 - title_class.length()) + " | ");
            System.out.print(avr_rating + "\n");
        }
        statement.close();
        connection.close();
    }

    public static void showSchools(int typeCondition) throws SQLException{
        Connection connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT id FROM scools"); // все id-ки школ
        StringBuilder topSchools = new StringBuilder(""); // через пробелы будем добавлять id-ки топ-школ
        Connection connection_2 = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS);
        Statement statement_2 = connection.createStatement();
        // ищем айдишники топ-школ
        if (typeCondition == 1) { // ищем школы, где более 60% учеников имеют оценку более 4.0
            while (resultSet.next()) { // перебираем все школы
                String school_id = resultSet.getString("id"); // id школы
                //всего учеников в школе
                ResultSet resultSet_2 = statement_2.executeQuery("SELECT COUNT(id) FROM (SELECT pupils.id FROM pupils JOIN teachers ON pupils.teacher_id = teachers.id JOIN scools ON teachers.school_id = scools.id WHERE scools.id = " + school_id + ");");
                resultSet_2.next();
                String countPupils = resultSet_2.getString("count");
                // сколько хорошистов
                resultSet_2 = statement_2.executeQuery("SELECT COUNT(id) FROM (SELECT pupils.id FROM pupils JOIN teachers ON pupils.teacher_id = teachers.id JOIN scools ON teachers.school_id = scools.id WHERE scools.id = " + school_id + " AND pupils.avr_rating >= 4.0);");
                resultSet_2.next();
                String countTopPupils = resultSet_2.getString("count");
                if ((Double.parseDouble(countTopPupils) / Double.parseDouble(countPupils) > 0.6)) {
                    topSchools.append(school_id + " ");
                }
            }
        } else if (typeCondition == 2) { // ищем школы со средним возрастом преподавателей более 35 лет
            while (resultSet.next()) {
                String school_id = resultSet.getString("id");
                // средний возраст в учителей в данной школе
                ResultSet resultSet_2 = statement_2.executeQuery("SELECT AVG(old) FROM (SELECT teachers.old FROM teachers JOIN scools ON teachers.school_id = scools.id WHERE scools.id = " + school_id + ");");
                resultSet_2.next();
                Double avgOld = resultSet_2.getDouble("avg");
                if (avgOld > 45.0) {
                    topSchools.append(school_id + " ");
                }
            }
        } else if (typeCondition == 3 || typeCondition == 4) { // Школы, где только мальчики/девочки
            String gender = (typeCondition == 3 ? "м" : "ж");
            while (resultSet.next()) {
                String school_id = resultSet.getString("id");
                // всего учеников в школе
                ResultSet resultSet_2 = statement_2.executeQuery("SELECT COUNT(id) FROM (SELECT pupils.id FROM pupils JOIN teachers ON pupils.teacher_id = teachers.id JOIN scools ON teachers.school_id = scools.id WHERE scools.id = " + school_id + ");");
                resultSet_2.next();
                int countPupils = resultSet_2.getInt("count");
                System.out.println(countPupils);
                // мальчиков
                resultSet_2 = statement_2.executeQuery("SELECT COUNT(id) FROM (SELECT pupils.id FROM pupils JOIN teachers ON pupils.teacher_id = teachers.id JOIN scools ON teachers.school_id = scools.id WHERE pupils.gender = '" + gender + "' AND scools.id = " + school_id + ");");
                resultSet_2.next();
                int countBoyPupils = resultSet_2.getInt("count");
                if (countPupils == countBoyPupils) {
                    topSchools.append(school_id + " ");
                }
            }
        }

        statement_2.close();
        connection_2.close();
        statement.close();
        connection.close();
        //выводим школы
        connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS);
        statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT scools.* FROM scools");
        System.out.println("  id" + " | " + "locality" + spaceN(24) + " | " + "number" + " | " + "type");
        System.out.println("----------------------------------------------------------------------------");
        while (resultSet.next()) {
            String id = resultSet.getString("id");
            if (!topSchools.toString().contains(id)) { // если это НЕ топ-школа
                continue;
            }
            String locality = resultSet.getString("locality");
            String number = resultSet.getString("number");
            String type = resultSet.getString("type");
            System.out.println(id + spaceN(4 - id.length()) + " | " + locality + spaceN(32 - locality.length()) + " | " + number + spaceN(6 - number.length()) + " | " + type);
        }
        statement.close();
        connection.close();

    }

    public static void delPupils(String id) throws SQLException{
        Connection connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS);
        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM pupils WHERE id = ?;");
        preparedStatement.setInt(1, Integer.parseInt(id));
        preparedStatement.executeUpdate();
        preparedStatement.close();
        connection.close();
    }

    public static void addPupils(String teacher_id, String name, String old, String gender, String title_class, String avr_rating) throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS);
        Statement statement = connection.createStatement();
        statement.executeUpdate("INSERT INTO pupils(teacher_id, name, old, gender, title_class, avr_rating) VALUES (" + teacher_id + ", " + "'" + name + "'" + ", " + old + ", " + "'" + gender + "'" + ", " + "'" + title_class + "'" + ", " +  avr_rating + ");");
        statement.close();
        connection.close();
    }

    public static void avgRating(String type) throws SQLException{
        Connection connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS);
        Statement statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery("SELECT AVG(avr_rating) FROM (SELECT pupils.*, scools.id, scools.locality, teachers.name, scools.number FROM pupils JOIN teachers ON pupils.teacher_id = teachers.id JOIN scools ON teachers.school_id = scools.id WHERE scools.type = '" + type + "')");
        resultSet.next();
        String arvRating = resultSet.getString(1);
        System.out.println(arvRating);
        statement.close();
        connection.close();

    }

    public static void yearLater() throws SQLException{
        // удаляем учеников 11-х классов
        Connection connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS);
        Statement statement = connection.createStatement();
        try {
            ResultSet resultSet = statement.executeQuery("SELECT id, title_class FROM pupils");
            while (resultSet.next()) {
                String title_class = resultSet.getString("title_class");
                //System.out.println(title_class);
                if (!title_class.substring(0, 2).equals("11")) {
                    continue;
                }
                String id = resultSet.getString("id");
                delPupils(id);
            }
        } catch (SQLException e)  {
            System.out.println("Одинадцатиклассников не обнаружено");
        }


        statement.close();
        connection.close();

        // повышаем возраст ученикам
        connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS);
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE pupils SET old = old + ?;");
        preparedStatement.setInt(1, 1);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        connection.close();
        // повышаем возраст учителей
        connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS);
        preparedStatement = connection.prepareStatement("UPDATE teachers SET old = old + ?;");
        preparedStatement.setInt(1, 1);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        connection.close();
        // повышаем стаж учителей
        connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS);
        preparedStatement = connection.prepareStatement("UPDATE teachers SET experience = experience + ?;");
        preparedStatement.setInt(1, 1);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        connection.close();
        // переводим учеников в старший класс
        connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS);
        statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT id, title_class FROM pupils");
        while (resultSet.next()) {
            String title_class = resultSet.getString("title_class");
            String id = resultSet.getString("id");
            // изменяем титл класса
            if (title_class.length() > 2) {
                int number = Integer.parseInt(title_class.substring(0, 2));
                title_class = String.valueOf(number + 1) + title_class.substring(2, title_class.length());
            } else {
                int number = Integer.parseInt(title_class.substring(0, 1));
                title_class = String.valueOf(number + 1) + title_class.substring(1, title_class.length());
            }
            preparedStatement = connection.prepareStatement("UPDATE pupils SET title_class = ? WHERE id = ?");
            preparedStatement.setString(1, title_class);
            preparedStatement.setInt(2, Integer.parseInt(id));
            preparedStatement.executeUpdate();
        }
        preparedStatement.close();
        statement.close();
        connection.close();
    }


    public static void main(String[] args) {
        // проверка возможности подключения
        checkDriver();
        checkDB();
        System.out.println("Подключение к базе данных | " + DATABASE_URL + "\n");
        Scanner scanner = new Scanner(System.in);
        String enter = "";
        while (!enter.equalsIgnoreCase("end")) {
            System.out.println("Выберете действие: ");
            System.out.println("1) Вывести информацию об учениках");
            System.out.println("2) Добавить нового ученика");
            System.out.println("3) Вывести среднюю оценку учеников городских школ");
            System.out.println("4) Вывести среднюю оценку учеников сельских школ");
            System.out.println("5) Вывести учеников, чей учитель имеет стаж 5 лет и менее");
            System.out.println("6) Вывести школы, где более 60% учеников имеют оценку более 4.0");
            System.out.println("7) Вывести школы со средним возрастом преподавателей более 45 лет");
            System.out.println("8) Школы, где только мальчики");
            System.out.println("9) Школы, где только девочки");
            System.out.println("10) Удалить ученика по ID");
            System.out.println("11) Пропустить год");
            enter = scanner.nextLine();
            // попытка открыть соединение с базой данных, которое java-закроет перед выходом из try-with-resources
            try {
                switch (enter) {
                    case "1" -> {
                        showPupils(1);
                        break;
                    }
                    case "2" -> {
                        System.out.print("Введите ID учителя, которому хотите доверить ученика: "); String teacher_id = scanner.nextLine();
                        System.out.print("Введите имя ученика (до 32 символов): "); String name = scanner.nextLine();
                        System.out.print("Введите возраст ученика: "); String old = String.valueOf(scanner.nextInt());
                        System.out.println("Введите пол ученика (м/ж):");
                        String gender = "";
                        while (!gender.equals("м") && !gender.equals("ж")) {
                            gender = scanner.nextLine();
                        }
                        System.out.println("Введите название класса ученика без пробела (например: 9Б): ");
                        String title_class = "";
                        while (!title_class.matches("\\d{1,2}[А-Я]{1}+")) {
                            title_class = scanner.nextLine();
                        }
                        System.out.print("Введите среднюю оценку ученика: ");
                        Double avr_rating = 1.0;
                        while ((avr_rating < 2.0) || (avr_rating) > 5.0) {
                            avr_rating = Double.parseDouble(scanner.nextLine());
                        }
                        addPupils(teacher_id, name, old, gender, title_class, String.valueOf(avr_rating));
                        break;
                    }
                    case "3" -> {
                        avgRating("городская");
                        break;
                    }
                    case "4" -> {
                        avgRating("сельская");
                        break;
                    }
                    case "5" -> {
                        showPupils(2);
                        break;
                    }
                    case "6" -> {
                        showSchools(1);
                        break;
                    }
                    case "7" -> {
                        showSchools(2);
                        break;
                    }
                    case "8" -> {
                        showSchools(3);
                        break;
                    }
                    case "9" -> {
                        showSchools(4);
                        break;
                    }
                    case "10" -> {
                        System.out.print("Введите id ученика: ");
                        String id = scanner.nextLine();
                        delPupils(id);
                        break;
                    }
                    case "11" -> {
                        yearLater();
                        break;
                    }
                    case "end" -> {break;}
                    default -> {
                        System.out.println("Некорректные данные, попробуйте ещё раз");
                    }
                }
            } catch (SQLException e) {
                // При открытии соединения, выполнении запросов могут возникать различные ошибки
                // Согласно стандарту SQL:2008 в ситуациях нарушения ограничений уникальности (в т.ч. дублирования данных) возникают ошибки соответствующие статусу (или дочерние ему): SQLState 23000 - Integrity Constraint Violation
                if (e.getSQLState().startsWith("23")){
                    System.out.println("Произошло дублирование данных");
                } else throw new RuntimeException(e);
            }

        }

    }

    // region // Проверка окружения и доступа к базе данных
    public static void checkDriver () {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("Нет JDBC-драйвера! Подключите JDBC-драйвер к проекту согласно инструкции.");
            throw new RuntimeException(e);
        }
    }

    public static void checkDB () {
        try {
            Connection connection = DriverManager.getConnection(DATABASE_URL, USER_NAME, DATABASE_PASS);
        } catch (SQLException e) {
            System.out.println("Нет базы данных! Проверьте имя базы, путь к базе или разверните локально резервную копию согласно инструкции");
            throw new RuntimeException(e);
        }
    }

}