/*
Name: Vaibhav Mahesh
Version: 1.0
Author: Mr. Di Tomasso
Date: Jan 14, 2026
Purpose: A combined Database and Classroom Rewards Tracker system.
*/


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class classroomRewardsTracker {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginScreen::new);
    }

    static class Student {
        String firstName;
        String lastName;
        int points;

        public Student(String fn, String ln, int pts) {
            this.firstName = fn;
            this.lastName = ln;
            this.points = pts;
        }

        public String getFullName() {
            return firstName + " " + lastName;
        }
    }

    static class CSVHandler {
        private static final String FILE = "students.csv";

        public static List<Student> loadAll() {
            List<Student> list = new ArrayList<>();
            File f = new File(FILE);
            if (!f.exists()) return list;

            try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",");
                    if (p.length < 3) continue;
                    list.add(new Student(p[0], p[1], Integer.parseInt(p[2])));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }

        public static void saveAll(List<Student> list) {
            try (FileWriter fw = new FileWriter(FILE)) {
                for (Student s : list) {
                    fw.write(s.firstName + "," + s.lastName + "," + s.points + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void addStudent(Student s) {
            List<Student> all = loadAll();
            all.add(s);
            saveAll(all);
        }

        public static Student findStudent(String fullName) {
            for (Student s : loadAll()) {
                if (s.getFullName().equalsIgnoreCase(fullName.trim())) {
                    return s;
                }
            }
            return null;
        }

        public static void addPoints(String fullName, int pts) {
            List<Student> all = loadAll();
            for (Student s : all) {
                if (s.getFullName().equalsIgnoreCase(fullName.trim())) {
                    s.points += pts;
                }
            }
            saveAll(all);
        }

        public static List<Student> top3() {
            List<Student> all = loadAll();
            all.sort((a, b) -> b.points - a.points);
            return all.subList(0, Math.min(3, all.size()));
        }

        public static List<Student> raffleEligible() {
            List<Student> eligible = new ArrayList<>();
            for (Student s : loadAll()) {
                if (s.points > 50) eligible.add(s);
            }
            return eligible;
        }
    }

    static class LoginScreen extends JFrame {
        public LoginScreen() {
            setTitle("School Points System - Login");
            setSize(400, 250);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new GridLayout(4, 1));

            JLabel title = new JLabel("Select Login Type", SwingConstants.CENTER);
            JButton teacherBtn = new JButton("Teacher / Staff");
            JButton studentBtn = new JButton("Student");
            JButton parentBtn = new JButton("Parent / Family");

            teacherBtn.addActionListener(e -> new TeacherDashboard());
            studentBtn.addActionListener(e -> new StudentLogin());
            parentBtn.addActionListener(e -> new ParentLogin());

            add(title);
            add(teacherBtn);
            add(studentBtn);
            add(parentBtn);

            setLocationRelativeTo(null);
            setVisible(true);
        }
    }

    static class TeacherDashboard extends JFrame {

        DefaultListModel<String> studentListModel = new DefaultListModel<>();
        JList<String> studentList = new JList<>(studentListModel);

        JTextField firstNameField = new JTextField(10);
        JTextField lastNameField = new JTextField(10);

        JLabel profileLabel = new JLabel("Select a student");
        JLabel pointsLabel = new JLabel("Points: ");
        JLabel raffleLabel = new JLabel("Raffle Eligible: NO");

        DefaultListModel<String> leaderboardModel = new DefaultListModel<>();
        DefaultListModel<String> raffleModel = new DefaultListModel<>();

        String currentSelectedStudent = null;

        public TeacherDashboard() {
            setTitle("Teacher Dashboard");
            setSize(500, 600);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            JPanel topPanel = new JPanel();
            topPanel.add(new JLabel("First:"));
            topPanel.add(firstNameField);
            topPanel.add(new JLabel("Last:"));
            topPanel.add(lastNameField);

            JButton addBtn = new JButton("Add Student");
            topPanel.add(addBtn);


            JButton addDeleteBtn = new JButton("Delete Student");
            topPanel.add(addBtn);


            add(topPanel, BorderLayout.NORTH);

            add(new JScrollPane(studentList), BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

            bottomPanel.add(profileLabel);
            bottomPanel.add(pointsLabel);
            bottomPanel.add(raffleLabel);

            JPanel awardPanel = new JPanel();
            JRadioButton participation = new JRadioButton("Participation");
            JRadioButton teamwork = new JRadioButton("Teamwork");
            JRadioButton onTask = new JRadioButton("On Task");
            JRadioButton behaviour = new JRadioButton("Behaviour");

            ButtonGroup group = new ButtonGroup();
            group.add(participation);
            group.add(teamwork);
            group.add(onTask);
            group.add(behaviour);

            awardPanel.add(participation);
            awardPanel.add(teamwork);
            awardPanel.add(onTask);
            awardPanel.add(behaviour);

            JButton awardBtn = new JButton("Award +5");
            awardPanel.add(awardBtn);

            bottomPanel.add(awardPanel);

            bottomPanel.add(new JLabel("Top 3 Students:"));
            JList<String> leaderboardList = new JList<>(leaderboardModel);
            bottomPanel.add(leaderboardList);

            bottomPanel.add(new JLabel("Raffle Eligible:"));
            JList<String> raffleList = new JList<>(raffleModel);
            bottomPanel.add(raffleList);

            add(bottomPanel, BorderLayout.SOUTH);

            loadStudentNames();
            refreshLeaderboardAndRaffle();

            addBtn.addActionListener(e -> addStudent());
            addDeleteBtn.addActionListener(e -> addStudent());


            studentList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    String selected = studentList.getSelectedValue();
                    if (selected != null) showStudentProfile(selected);
                }
            });

            awardBtn.addActionListener(e -> {
                if (currentSelectedStudent == null) {
                    JOptionPane.showMessageDialog(this, "Select a student first.");
                    return;
                }
                CSVHandler.addPoints(currentSelectedStudent, 5);
                showStudentProfile(currentSelectedStudent);
                refreshLeaderboardAndRaffle();
            });

            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void addStudent() {
            String fn = firstNameField.getText().trim();
            String ln = lastNameField.getText().trim();

            if (fn.isEmpty() || ln.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter both first and last name.");
                return;
            }

            Student existing = CSVHandler.findStudent(fn + " " + ln);
            if (existing != null) {
                JOptionPane.showMessageDialog(this, "Student already exists.");
                return;
            }

            CSVHandler.addStudent(new Student(fn, ln, 0));
            loadStudentNames();

            firstNameField.setText("");
            lastNameField.setText("");
        }

        private void loadStudentNames() {
            studentListModel.clear();
            for (Student s : CSVHandler.loadAll()) {
                studentListModel.addElement(s.getFullName());
            }
        }

        private void showStudentProfile(String fullName) {
            currentSelectedStudent = fullName;
             Student s = CSVHandler.findStudent(fullName);

            if (s == null) return;

            profileLabel.setText("Name: " + s.getFullName());
            pointsLabel.setText("Points: " + s.points);
            raffleLabel.setText("Raffle Eligible: " + (s.points > 50 ? "YES" : "NO"));
        }

        private void refreshLeaderboardAndRaffle() {
            leaderboardModel.clear();
            for (Student s : CSVHandler.top3()) {
                leaderboardModel.addElement(s.getFullName() + " - " + s.points + " pts");
            }

            raffleModel.clear();
            for (Student s : CSVHandler.raffleEligible()) {
                raffleModel.addElement(s.getFullName() + " - " + s.points + " pts");
            }
        }
    }

    static class StudentLogin extends JFrame {
        public StudentLogin() {
            setTitle("Student Login");
            setSize(300, 150);
            setLayout(new GridLayout(3, 1));

            JTextField nameField = new JTextField();
            JButton loginBtn = new JButton("Enter");

            loginBtn.addActionListener(e -> new StudentProfile(nameField.getText()));

            add(new JLabel("Enter your full name:", SwingConstants.CENTER));
            add(nameField);
            add(loginBtn);

            setLocationRelativeTo(null);
            setVisible(true);
        }
    }

    static class StudentProfile extends JFrame {

        DefaultListModel<String> leaderboardModel = new DefaultListModel<>();
        DefaultListModel<String> raffleModel = new DefaultListModel<>();

        public StudentProfile(String fullName) {
            setTitle("Student Profile");
            setSize(600, 400);
            setLayout(new BorderLayout());

            Student s = CSVHandler.findStudent(fullName);

            JPanel centerPanel = new JPanel(new GridLayout(3, 1));
            if (s == null) {
                centerPanel.add(new JLabel("Student not found", SwingConstants.CENTER));
            } else {
                centerPanel.add(new JLabel("Name: " + s.getFullName(), SwingConstants.CENTER));
                centerPanel.add(new JLabel("Points: " + s.points, SwingConstants.CENTER));
                centerPanel.add(new JLabel("Raffle Eligible: " + (s.points > 50 ? "YES" : "NO"), SwingConstants.CENTER));
            }

            add(centerPanel, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel(new GridLayout(1, 2));

            JPanel leaderboardPanel = new JPanel(new BorderLayout());
            leaderboardPanel.add(new JLabel("Top 3 Students:"), BorderLayout.NORTH);
            JList<String> leaderboardList = new JList<>(leaderboardModel);
            leaderboardPanel.add(new JScrollPane(leaderboardList), BorderLayout.CENTER);

            JPanel rafflePanel = new JPanel(new BorderLayout());
            rafflePanel.add(new JLabel("Raffle Eligible:"), BorderLayout.NORTH);
            JList<String> raffleList = new JList<>(raffleModel);
            rafflePanel.add(new JScrollPane(raffleList), BorderLayout.CENTER);

            bottomPanel.add(leaderboardPanel);
            bottomPanel.add(rafflePanel);

            add(bottomPanel, BorderLayout.SOUTH);

            refreshLeaderboardAndRaffle();

            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void refreshLeaderboardAndRaffle() {
            leaderboardModel.clear();
            for (Student s : CSVHandler.top3()) {
                leaderboardModel.addElement(s.getFullName() + " - " + s.points + " pts");
            }

            raffleModel.clear();
            for (Student s : CSVHandler.raffleEligible()) {
                raffleModel.addElement(s.getFullName() + " - " + s.points + " pts");
            }
        }
    }

    static class ParentLogin extends JFrame {
        public ParentLogin() {
            setTitle("Parent Login");
            setSize(300, 150);
            setLayout(new GridLayout(3, 1));

            JTextField nameField = new JTextField();
            JButton loginBtn = new JButton("Enter");

            loginBtn.addActionListener(e -> new ParentProfile(nameField.getText()));

            add(new JLabel("Enter student full name:", SwingConstants.CENTER));
            add(nameField);
            add(loginBtn);

            setLocationRelativeTo(null);
            setVisible(true);
        }
    }

    static class ParentProfile extends JFrame {

        DefaultListModel<String> leaderboardModel = new DefaultListModel<>();
        DefaultListModel<String> raffleModel = new DefaultListModel<>();

        public ParentProfile(String fullName) {
            setTitle("Parent View");
            setSize(600, 400);
            setLayout(new BorderLayout());

            Student s = CSVHandler.findStudent(fullName);

            JPanel centerPanel = new JPanel(new GridLayout(3, 1));
            if (s == null) {
                centerPanel.add(new JLabel("Student not found", SwingConstants.CENTER));
            } else {
                centerPanel.add(new JLabel("Student: " + s.getFullName(), SwingConstants.CENTER));
                centerPanel.add(new JLabel("Points: " + s.points, SwingConstants.CENTER));
                centerPanel.add(new JLabel("Raffle Eligible: " + (s.points > 50 ? "YES" : "NO"), SwingConstants.CENTER));
            }

            add(centerPanel, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel(new GridLayout(1, 2));

            JPanel leaderboardPanel = new JPanel(new BorderLayout());
            leaderboardPanel.add(new JLabel("Top 3 Students:"), BorderLayout.NORTH);
            JList<String> leaderboardList = new JList<>(leaderboardModel);
            leaderboardPanel.add(new JScrollPane(leaderboardList), BorderLayout.CENTER);

            JPanel rafflePanel = new JPanel(new BorderLayout());
            rafflePanel.add(new JLabel("Raffle Eligible:"), BorderLayout.NORTH);
            JList<String> raffleList = new JList<>(raffleModel);
            rafflePanel.add(new JScrollPane(raffleList), BorderLayout.CENTER);

            bottomPanel.add(leaderboardPanel);
            bottomPanel.add(rafflePanel);

            add(bottomPanel, BorderLayout.SOUTH);

            refreshLeaderboardAndRaffle();

            setLocationRelativeTo(null);
            setVisible(true);
        }

        private void refreshLeaderboardAndRaffle() {
            leaderboardModel.clear();
            for (Student s : CSVHandler.top3()) {
                leaderboardModel.addElement(s.getFullName() + " - " + s.points + " pts");
            }

            raffleModel.clear();
            for (Student s : CSVHandler.raffleEligible()) {
                raffleModel.addElement(s.getFullName() + " - " + s.points + " pts");
            }
        }
    }
}


