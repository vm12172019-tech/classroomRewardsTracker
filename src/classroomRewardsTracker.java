import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;


public class classroomRewardsTracker {


   public static void main(String[] args) {


       // DELETE ALL DATA ONLY WHEN PROGRAM FULLY CLOSES
       Runtime.getRuntime().addShutdownHook(new Thread(() -> {
           File f1 = new File("students.csv");
           if (f1.exists()) f1.delete();
           File f2 = new File("profiles.csv");
           if (f2.exists()) f2.delete();
       }));


       SwingUtilities.invokeLater(() -> {
           File profilesFile = new File("profiles.csv");
           if (!profilesFile.exists() || profilesFile.length() == 0) {
               new ProfileCreationScreen();
           } else {
               new LoginScreen();
           }
       });
   }


   // -------------------- STUDENT DATA --------------------


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


       public static void deleteStudent(String fullName) {
           List<Student> all = loadAll();
           all.removeIf(s -> s.getFullName().equalsIgnoreCase(fullName.trim()));
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


   // -------------------- PROFILE DATA --------------------


   static class Profile {
       String role;       // teacher, student, parent
       String fullName;   // person name
       String username;   // jsmith.teacher, abrown.student, mlee.parent
       String linkedName; // teacher: school name; student: "None"; parent: student name


       public Profile(String role, String fullName, String username, String linkedName) {
           this.role = role;
           this.fullName = fullName;
           this.username = username;
           this.linkedName = linkedName;
       }
   }


   static class ProfileHandler {
       private static final String FILE = "profiles.csv";


       public static List<Profile> loadAll() {
           List<Profile> list = new ArrayList<>();
           File f = new File(FILE);
           if (!f.exists()) return list;


           try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
               String line;
               while ((line = br.readLine()) != null) {
                   String[] p = line.split(",", -1);
                   if (p.length < 4) continue;
                   list.add(new Profile(p[0], p[1], p[2], p[3]));
               }
           } catch (Exception e) {
               e.printStackTrace();
           }
           return list;
       }


       public static void saveAll(List<Profile> list) {
           try (FileWriter fw = new FileWriter(FILE)) {
               for (Profile p : list) {
                   fw.write(p.role + "," + p.fullName + "," + p.username + "," + p.linkedName + "\n");
               }
           } catch (Exception e) {
               e.printStackTrace();
           }
       }


       public static void addProfile(Profile p) {
           List<Profile> all = loadAll();
           all.add(p);
           saveAll(all);
       }


       public static Profile findByUsernameAndRole(String username, String role) {
           for (Profile p : loadAll()) {
               if (p.username.equalsIgnoreCase(username.trim()) && p.role.equalsIgnoreCase(role)) {
                   return p;
               }
           }
           return null;
       }


       public static Profile findParentForStudent(String username, String studentName) {
           for (Profile p : loadAll()) {
               if (p.role.equalsIgnoreCase("parent")
                       && p.username.equalsIgnoreCase(username.trim())
                       && p.linkedName.equalsIgnoreCase(studentName.trim())) {
                   return p;
               }
           }
           return null;
       }


       public static String generateUsername(String fullName, String role) {
           String[] parts = fullName.trim().split("\\s+");
           if (parts.length == 0) return "";
           String first = parts[0].toLowerCase();
           String last = parts[parts.length - 1].toLowerCase();
           String base = (first.substring(0, 1) + last);
           String suffix = "";
           if (role.equalsIgnoreCase("teacher")) suffix = ".teacher";
           else if (role.equalsIgnoreCase("student")) suffix = ".student";
           else if (role.equalsIgnoreCase("parent")) suffix = ".parent";
           return base + suffix;
       }
   }


   // -------------------- PROFILE CREATION SCREEN --------------------


   static class ProfileCreationScreen extends JFrame {
       public ProfileCreationScreen() {
           setTitle("Create Profiles");
           setSize(400, 250);
           setDefaultCloseOperation(EXIT_ON_CLOSE);
           setLayout(new GridLayout(6, 1));


           JLabel roleLabel = new JLabel("Select role:", SwingConstants.CENTER);
           String[] roles = {"Teacher", "Student", "Parent"};
           JComboBox<String> roleBox = new JComboBox<>(roles);


           JTextField nameField = new JTextField();
           JLabel secondLabel = new JLabel("Extra info:", SwingConstants.CENTER);
           JTextField secondField = new JTextField();


           JButton createBtn = new JButton("Create Profile");
           JButton goLoginBtn = new JButton("Go to Login");


           add(roleLabel);
           add(roleBox);
           add(new JLabel("Full name:", SwingConstants.CENTER));
           add(nameField);
           add(secondLabel);
           add(secondField);


           JPanel bottomPanel = new JPanel();
           bottomPanel.add(createBtn);
           bottomPanel.add(goLoginBtn);
           add(bottomPanel);


           roleBox.addActionListener(e -> {
               String role = ((String) roleBox.getSelectedItem()).toLowerCase();
               if (role.equals("teacher")) {
                   secondLabel.setText("School name:");
               } else if (role.equals("student")) {
                   secondLabel.setText("Extra (ignored):");
               } else if (role.equals("parent")) {
                   secondLabel.setText("Student full name:");
               }
           });


           createBtn.addActionListener(e -> {
               String role = ((String) roleBox.getSelectedItem()).toLowerCase();
               String fullName = nameField.getText().trim();
               String extra = secondField.getText().trim();


               if (fullName.isEmpty()) {
                   JOptionPane.showMessageDialog(this, "Enter full name.");
                   return;
               }


               if (role.equals("teacher")) {
                   if (extra.isEmpty()) {
                       JOptionPane.showMessageDialog(this, "Enter school name.");
                       return;
                   }
                   String username = ProfileHandler.generateUsername(fullName, "teacher");
                   ProfileHandler.addProfile(new Profile("teacher", fullName, username, extra));
                   JOptionPane.showMessageDialog(this, "Teacher profile created. Username: " + username);
               } else if (role.equals("student")) {
                   String username = ProfileHandler.generateUsername(fullName, "student");
                   ProfileHandler.addProfile(new Profile("student", fullName, username, "None"));
                   JOptionPane.showMessageDialog(this, "Student profile created. Username: " + username);
               } else if (role.equals("parent")) {
                   if (extra.isEmpty()) {
                       JOptionPane.showMessageDialog(this, "Enter student full name.");
                       return;
                   }
                   String username = ProfileHandler.generateUsername(fullName, "parent");
                   ProfileHandler.addProfile(new Profile("parent", fullName, username, extra));
                   JOptionPane.showMessageDialog(this, "Parent profile created. Username: " + username);
               }


               nameField.setText("");
               secondField.setText("");
           });


           goLoginBtn.addActionListener(e -> {
               dispose();
               new LoginScreen();
           });


           setLocationRelativeTo(null);
           setVisible(true);
       }
   }


   // -------------------- LOGIN SCREEN --------------------


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


           teacherBtn.addActionListener(e -> {
               dispose();
               new TeacherLogin();
           });
           studentBtn.addActionListener(e -> {
               dispose();
               new StudentLogin();
           });
           parentBtn.addActionListener(e -> {
               dispose();
               new ParentLogin();
           });


           add(title);
           add(teacherBtn);
           add(studentBtn);
           add(parentBtn);


           setLocationRelativeTo(null);
           setVisible(true);
       }
   }


   // -------------------- TEACHER LOGIN --------------------


   static class TeacherLogin extends JFrame {
       public TeacherLogin() {
           setTitle("Teacher Login");
           setSize(300, 200);
           setLayout(new GridLayout(4, 1));


           JTextField usernameField = new JTextField();
           JButton loginBtn = new JButton("Enter");
           JButton backBtn = new JButton("Back");


           loginBtn.addActionListener(e -> {
               String username = usernameField.getText().trim();
               if (!username.endsWith(".teacher")) {
                   JOptionPane.showMessageDialog(this, "Username must end with .teacher");
                   return;
               }
               Profile p = ProfileHandler.findByUsernameAndRole(username, "teacher");
               if (p == null) {
                   JOptionPane.showMessageDialog(this, "Teacher profile not found.");
                   return;
               }
               dispose();
               new TeacherDashboard();
           });


           backBtn.addActionListener(e -> {
               dispose();
               new LoginScreen();
           });


           add(new JLabel("Enter teacher username:", SwingConstants.CENTER));
           add(usernameField);
           add(loginBtn);
           add(backBtn);


           setLocationRelativeTo(null);
           setVisible(true);
       }
   }


   // -------------------- TEACHER DASHBOARD --------------------


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


           JButton backBtn = new JButton("Back");
           backBtn.addActionListener(e -> {
               dispose();
               new LoginScreen();
           });
           topPanel.add(backBtn);


           topPanel.add(new JLabel("First:"));
           topPanel.add(firstNameField);
           topPanel.add(new JLabel("Last:"));
           topPanel.add(lastNameField);


           JButton addBtn = new JButton("Add Student");
           JButton deleteBtn = new JButton("Delete Student");
           topPanel.add(addBtn);
           topPanel.add(deleteBtn);


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


           deleteBtn.addActionListener(e -> {
               if (currentSelectedStudent == null) {
                   JOptionPane.showMessageDialog(this, "Select a student first.");
                   return;
               }


               int confirm = JOptionPane.showConfirmDialog(
                       this,
                       "Delete " + currentSelectedStudent + "?",
                       "Confirm Delete",
                       JOptionPane.YES_NO_OPTION
               );


               if (confirm == JOptionPane.YES_OPTION) {
                   CSVHandler.deleteStudent(currentSelectedStudent);
                   loadStudentNames();
                   refreshLeaderboardAndRaffle();
                   profileLabel.setText("Select a student");
                   pointsLabel.setText("Points: ");
                   raffleLabel.setText("Raffle Eligible: NO");
                   currentSelectedStudent = null;
               }
           });


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


   // -------------------- STUDENT LOGIN --------------------


   static class StudentLogin extends JFrame {
       public StudentLogin() {
           setTitle("Student Login");
           setSize(300, 200);
           setLayout(new GridLayout(4, 1));


           JTextField usernameField = new JTextField();
           JButton loginBtn = new JButton("Enter");
           JButton backBtn = new JButton("Back");


           loginBtn.addActionListener(e -> {
               String username = usernameField.getText().trim();
               if (!username.endsWith(".student")) {
                   JOptionPane.showMessageDialog(this, "Username must end with .student");
                   return;
               }
               Profile p = ProfileHandler.findByUsernameAndRole(username, "student");
               if (p == null) {
                   JOptionPane.showMessageDialog(this, "Student profile not found.");
                   return;
               }
               dispose();
               new StudentProfile(p.fullName);
           });


           backBtn.addActionListener(e -> {
               dispose();
               new LoginScreen();
           });


           add(new JLabel("Enter student username:", SwingConstants.CENTER));
           add(usernameField);
           add(loginBtn);
           add(backBtn);


           setLocationRelativeTo(null);
           setVisible(true);
       }
   }


   // -------------------- STUDENT PROFILE --------------------


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


   // -------------------- PARENT LOGIN --------------------


   static class ParentLogin extends JFrame {
       public ParentLogin() {
           setTitle("Parent Login");
           setSize(300, 200);
           setLayout(new GridLayout(5, 1));


           JTextField usernameField = new JTextField();
           JTextField studentNameField = new JTextField();
           JButton loginBtn = new JButton("Enter");
           JButton backBtn = new JButton("Back");


           loginBtn.addActionListener(e -> {
               String username = usernameField.getText().trim();
               String studentName = studentNameField.getText().trim();


               if (!username.endsWith(".parent")) {
                   JOptionPane.showMessageDialog(this, "Username must end with .parent");
                   return;
               }
               if (studentName.isEmpty()) {
                   JOptionPane.showMessageDialog(this, "Enter student full name.");
                   return;
               }


               Profile p = ProfileHandler.findParentForStudent(username, studentName);
               if (p == null) {
                   JOptionPane.showMessageDialog(this, "Parent profile or linked student not found.");
                   return;
               }


               dispose();
               new ParentProfile(studentName);
           });


           backBtn.addActionListener(e -> {
               dispose();
               new LoginScreen();
           });


           add(new JLabel("Enter parent username:", SwingConstants.CENTER));
           add(usernameField);
           add(new JLabel("Enter student full name:", SwingConstants.CENTER));
           add(studentNameField);
           add(loginBtn);
           add(backBtn);


           setLocationRelativeTo(null);
           setVisible(true);
       }
   }


   // -------------------- PARENT PROFILE --------------------


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


