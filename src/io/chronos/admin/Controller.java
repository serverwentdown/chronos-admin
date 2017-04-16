package io.chronos.admin;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.util.Callback;

import javax.print.DocFlavor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static io.chronos.admin.Controller.alertBadInput;

public class Controller {
    public TextField connect_host;
    public TextField connect_port;
    public TextField connect_user;
    public TextField connect_pass;
    public Button connect_go;
    public Label connect_status;
    public TableView<List<String>> schools_table;
    public TextField schools_search;
    public TextField add_school_name;
    public TextField add_school_domain;
    public Button add_school_go;
    public TextField delete_school_id;
    public Button delete_school_go;
    public TextField users_school;
    public TableView<List<String>> users_table;
    public Button users_go;
    public TextField modify_user_school;
    public TextField modify_user_id;
    public Button modify_user_load;
    public TextField modify_user_name;
    public TextField modify_user_email;
    public TextField modify_user_role;
    public Button modify_user_go;
    public TextField connect_database;
    public TextField add_user_school;
    public TextField add_user_name;
    public TextField add_user_role;
    public TextField add_user_email;
    public Button add_user_go;

    private Connection c;

    public void initialize() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void connect(ActionEvent actionEvent) {
        try {
            String url = "jdbc:mysql://" + connect_host.getText() + ":" + connect_port.getText() + "/" + connect_database.getText();
            c = DriverManager.getConnection(url, connect_user.getText(), connect_pass.getText());
            connect_status.setText("Connected to " + url);
        } catch (SQLException e) {
            e.printStackTrace();
            connect_status.setText("Unable to connect");
        }
    }

    private static void loadResultsIntoTable(ResultSet rs, ResultSetMetaData md, TableView tv) throws SQLException {
        tv.getColumns().clear();
        TableColumn[] cols = new TableColumn[md.getColumnCount()];
        for (int i = 0; i < md.getColumnCount(); i++) {
            cols[i] = new TableColumn(md.getColumnName(i + 1));
            int finalI = i;
            cols[i].setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ArrayList<String>, String>, ObservableValue>() {
                @Override
                public ObservableValue call(TableColumn.CellDataFeatures<ArrayList<String>, String> data) {
                    return new ReadOnlyStringWrapper(data.getValue().get(finalI));
                }
            });
        }
        tv.getColumns().addAll(cols);

        tv.getItems().clear();
        ArrayList<ArrayList<String>> rows = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            for (int i = 0; i < md.getColumnCount(); i++) {
                row.add(rs.getString(i + 1));
            }
            rows.add(row);
        }
        tv.getItems().addAll(rows);
    }

    public void queryUsers(ActionEvent actionEvent) {
        try {
            PreparedStatement ps = null;
            if (users_school.getText().length() > 0) {
                ps = c.prepareStatement("SELECT * FROM user WHERE user.school = ?");
                ps.setInt(1, Integer.parseInt(users_school.getText()));
            } else {
                ps = c.prepareStatement("SELECT * FROM user");
            }
            ps.execute();
            loadResultsIntoTable(ps.getResultSet(), ps.getMetaData(), users_table);
        } catch (NumberFormatException e) {
            alertBadInput();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifyUserLoad(ActionEvent actionEvent) {
        try {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM user WHERE user.school = ? AND user.id = ?");
            ps.setInt(1, Integer.parseInt(modify_user_school.getText()));
            ps.setInt(2, Integer.parseInt(modify_user_id.getText()));
            ps.execute();

            ResultSet rs = ps.getResultSet();
            if (rs.next()) {
                modify_user_name.setText(rs.getString("name"));
                modify_user_email.setText(rs.getString("email"));
                modify_user_role.setText(rs.getString("role"));
            }
        } catch (NumberFormatException e) {
            alertBadInput();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifyUser(ActionEvent actionEvent) {
        try {
            PreparedStatement ps = c.prepareStatement("UPDATE user SET user.name = ?, user.email = ?, user.role = ? WHERE user.school = ? AND user.id = ?");
            ps.setString(1, modify_user_name.getText());
            ps.setString(2, modify_user_email.getText());
            ps.setString(3, modify_user_role.getText());
            ps.setInt(4, Integer.parseInt(modify_user_school.getText()));
            ps.setInt(5, Integer.parseInt(modify_user_id.getText()));
            ps.execute();

            alertUpdateDone();
        } catch (NumberFormatException e) {
            alertBadInput();
        } catch (SQLException e) {
            e.printStackTrace();
            alertBadInput();
        }
    }

    public void addUser(ActionEvent actionEvent) {
        try {
            PreparedStatement ps = c.prepareStatement("INSERT INTO user (name, email, role, school) VALUES (?, ?, ?, ?)");
            ps.setString(1, add_user_name.getText());
            ps.setString(2, add_user_email.getText());
            ps.setString(3, add_user_role.getText());
            ps.setInt(4, Integer.parseInt(add_user_school.getText()));
            ps.execute();

            alertInsertDone();
        } catch (NumberFormatException e) {
            alertBadInput();
        } catch (SQLException e) {
            e.printStackTrace();
            alertBadInput();
        }
    }

    public void querySchools(ActionEvent actionEvent) {
        try {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM school WHERE school.name LIKE ?");
            ps.setString(1,
                    "%" +
                    schools_search.getText()
                            .replace("!", "!!")
                            .replace("%", "!%")
                            .replace("_", "!_")
                            .replace("[", "![")
                    + "%"
            );
            ps.execute();
            loadResultsIntoTable(ps.getResultSet(), ps.getMetaData(), schools_table);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addSchool(ActionEvent actionEvent) {
        try {
            PreparedStatement ps = c.prepareStatement("INSERT INTO school (name, domain) VALUES (?, ?)");
            ps.setString(1, add_school_name.getText());
            ps.setString(2, add_school_domain.getText());
            ps.execute();

            alertInsertDone();
        } catch (SQLException e) {
            e.printStackTrace();
            alertBadInput();
        }
    }

    public void deleteSchool(Event event) {
        try {
            PreparedStatement ps = c.prepareStatement("DELETE FROM school WHERE school.id = ?");
            ps.setInt(1, Integer.parseInt(delete_school_id.getText()));
            ps.execute();

            alertDeleteDone();
        } catch (NumberFormatException e) {
            alertBadInput();
        } catch (SQLException e) {
            e.printStackTrace();
            alertBadInput();
        }
    }

    public static void alertBadInput() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Woof!");
        alert.setHeaderText("Woof!");
        alert.setContentText("Bad input");
        alert.showAndWait();
    }

    public static void alertInsertDone() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Woof!");
        alert.setHeaderText("Woof!");
        alert.setContentText("Insert done!");
        alert.showAndWait();
    }
    public static void alertUpdateDone() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Woof!");
        alert.setHeaderText("Woof!");
        alert.setContentText("Update done!");
        alert.showAndWait();
    }
    public static void alertDeleteDone() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Woof!");
        alert.setHeaderText("Woof!");
        alert.setContentText("Delete done!");
        alert.showAndWait();
    }
}
