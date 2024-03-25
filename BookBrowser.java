// -----------------------------------------------------
// Assignment 2
// Question: Exception Handling and File I/O
// Written by: Neha Sanjay Deshmukh, Student ID: 40221804
// -----------------------------------------------------
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.util.Scanner;

/**
 * The BookBrowser class provides functionalities to manage books and browse through their information.
 */
public class BookBrowser {

    static final String[] Genre_Codes = { "CCB", "HCB", "MTV", "MRB", "NEB", "OTR", "SSM", "TPA" };

    protected static final String[] CSV_OP_File_Name = { "Cartoons_Comics.csv", "Hobbies_Collectibles.csv",
            "Movies_TV_Books.csv", "Music_Radio_Books.csv", "Nostalgia_Eclectic_Books.csv", "Old_Time_Radio_Books.csv",
            "Sports_Sports_Memorabilia.csv", "Trains_Planes_Automobiles.csv" };

    protected static UserDefinedMaps Genre_Codes_VS_CSV_OP_File_Name = new UserDefinedMaps(Genre_Codes, CSV_OP_File_Name);

    protected static final String[] SER_OP_File_Name = { "Cartoons_Comics.csv.ser", "Hobbies_Collectibles.csv.ser",
    "Movies_TV_Books.csv.ser", "Music_Radio_Books.csv.ser", "Nostalgia_Eclectic_Books.csv.ser",
    "Old_Time_Radio_Books.csv.ser", "Sports_Sports_Memorabilia.csv.ser", "Trains_Planes_Automobiles.csv.ser" };

    static final UserDefinedMaps CSV_RECORD_Count = new UserDefinedMaps(Genre_Codes.length);

    static {
        // Initialize CSV record count for each genre
        for (int i = 0; i < CSV_OP_File_Name.length; i++) {
            CSV_RECORD_Count.putValue(CSV_OP_File_Name[i], 0);
        }
    }

    public static void main(String[] args) {
        do_part1();
        do_part2();
        do_part3();
    }

    /**
     * Part 1: Read input file names, validate syntax, and write to CSV files
     */

    public static void do_part1() {
        final PrintWriter[] printWriters = Util.getWriter(CSV_OP_File_Name);
        final UserDefinedMaps userDefinedMaps = new UserDefinedMaps(Genre_Codes, printWriters);

        BufferedReader bufferedReader = null;
        try {
            // Read input file containing file count and file names
            bufferedReader = new BufferedReader(new FileReader("part1_input_file_names.txt"));
            int filecount = Integer.parseInt(bufferedReader.readLine());
            // Validate syntax and write records to appropriate CSV files
            Util.validatesyntax(filecount, bufferedReader, userDefinedMaps);
        } catch (final IOException e) {
            System.out.println("Error reading file count");
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (final IOException e) {
                    System.out.println("Error closing input file");
                }
            }
            for (PrintWriter writer : printWriters) {
                writer.close();
            }
        }
    }

    /**
     * Part 2: Validate semantic errors and write to binary files
     */
    public static void do_part2() {
        final File semanticerrorfile = new File("semantic_errors.txt");
        for (int i = 0; i < CSV_OP_File_Name.length; i++) {

            int totalbooks = (int) CSV_RECORD_Count.getValue(CSV_OP_File_Name[i]);
            int bookcounter = 0;
            Book[] books = new Book[totalbooks];

            BufferedReader reader = null;
            try {
                // Read CSV file
                final File file = new File(CSV_OP_File_Name[i]);
                reader = new BufferedReader(new FileReader(file));

                // Read each line and validate semantic errors and create Book objects
                String line;
                while ((line = reader.readLine()) != null) {
                    // Extract fields from the line
                    final String[] fields = Util.extractField(line);

                    try {
                        Util.validsemanticerrors(fields);
                    } catch (BadIsbn10Exception | BadIsbn13Exception | BadPriceException | BadYearException e) {
                        Util.logError(semanticerrorfile, file, e.getMessage(), line);
                        totalbooks--;
                        continue;
                    }

                    // Create Book object
                    books[bookcounter] = Util.createB(fields);
                    bookcounter++;
                }

                // Serialize books
                books = Util.transfer(books, totalbooks);
                Util.serializeBook(CSV_OP_File_Name[i], books);
            } catch (IOException e) {
                System.err.println("Error reading file: " + CSV_OP_File_Name[i]);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        System.err.println("Error closing file: " + CSV_OP_File_Name[i]);
                    }
                }
            }
        }
    }

    /**
     * Part 3: Deserialize books and start the Book Search UI
     */
    public static void do_part3() {
        final Book[][] books = new Book[CSV_OP_File_Name.length][];
        for (int i = 0; i < CSV_OP_File_Name.length; i++) {
            // Deserialize books for each genre
            Book[] genrebooks = Util.deserializebook(CSV_OP_File_Name[i]);
            books[i] = genrebooks;
        }
        BooksSearchUI.startOp(books);
    }
}

// Class for the Book Search User Interface
class BooksSearchUI {
    private static final Scanner sc = new Scanner(System.in);

    private BooksSearchUI() {

    }


    static void startOp(final Book[][] books) {
        int selectedfile = 0;
        while (true) {
            final String selectedfilename = BookBrowser.CSV_OP_File_Name[selectedfile] + ".ser";
            printMenu(selectedfilename, books[selectedfile].length);

            // Get user choice
            System.out.println("Enter Your Choice: ");
            String ch = sc.nextLine();

            // Process user choice
            switch (ch) {
                case "v":
                    // View books in the selected file
                    view(selectedfilename, books[selectedfile]);
                    break;
                case "s":
                    // Select a file to view
                    printsubmenu(books);
                    final int i = Integer.parseInt(sc.nextLine()) - 1;
                    if (i == 8) {
                        break;
                    } else if (i < 0 || i >= BookBrowser.CSV_OP_File_Name.length) {
                        System.out.println("Invalid selection");
                        break;
                    }
                    selectedfile = i;
                    break;
                case "x":
                case "X":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice");
                    break;
            }
        }
    }
    /**
     * Prints the main menu options for book browsing.
     * @param selectedFile The name of the selected CSV file.
     * @param numRecords The number of records in the selected file.
     */

    private static void printMenu(String selectedFile, int numRecords) {
        System.out.println("----------------------------");
        System.out.println("          Main Menu         ");
        System.out.println("----------------------------");
        System.out.println("v View the selected file: " + selectedFile + " (" + numRecords + " records)");
        System.out.println("s Select a file to view");
        System.out.println("x Exit");
        System.out.println("----------------------------");
    }

    /**
     * Displays the contents of a specific file based on user selection.
     * @param sFileName The name of the selected CSV file.
     * @param b The array of books in the selected file.
     */
    private static void view(final String sFileName, final Book[] b) {
        System.out.println("viewing: " + sFileName);
        int currentposition = 0;
        int rng = 0;
        while (true) {
            System.out.println("Enter the direction & number of books to browse : ");
            int in = sc.nextInt();
            rng = Math.min(b.length - 1, Math.max(0, currentposition + in + (in > 0 ? -1 : 1)));
            if (in == 0) { // Exit
                return;
            } else { // Print books in the specified range
                if (rng >= currentposition) {
                    printB(b, currentposition, rng);
                } else {
                    printB(b, rng, currentposition);
                }
            }
            currentposition = rng;
        }
    }

    /**
     * Prints a range of books from the specified starting index to the ending index.
     * @param b The array of books.
     * @param st The starting index of the range.
     * @param end The ending index of the range.
     */
    private static void printB(final Book[] b, final int st, final int end) {
        for (int i = st; i <= end; i++) {
            System.err.println(
                    "----------------------------" + "----------------------------" + "----------------------------" + "----------------------------" + "----------------------------" + "----------------------------" + "----------------------------");
            System.out.println((i + 1) + " ===> " + b[i].toString());
        }
        System.out.println(
                "----------------------------" + "----------------------------" + "----------------------------" + "----------------------------" + "----------------------------" + "----------------------------" + "----------------------------");
    }

    /**
     * Prints the sub-menu options for selecting a file to view.
     * @param books The 2D array containing books from different CSV files.
     */
    private static void printsubmenu(final Book[][] books) {
        System.out.println("----------------------------");
        System.out.println("        File Sub-Menu       ");
        System.out.println("----------------------------");
        for (int i = 0; i < BookBrowser.CSV_OP_File_Name.length; i++) {
            System.out
                    .println((i + 1) + " " + BookBrowser.CSV_OP_File_Name[i] + ".ser (" + books[i].length + " records)");
        }
        System.out.println(BookBrowser.CSV_OP_File_Name.length + 1 + " Exit");
        System.out.println("----------------------------");
    }
}

// Utility class for various helper methods
class Util {
    /**
     * Creates a Book object from the provided fields.
     * @param fields An array containing the fields of the book.
     * @return The created Book object.
     */
    static Book createB(String[] fields) {
        // Extract fields from the array and create a Book object
        String Title = fields[0].trim();
        String Author = fields[1].trim();
        double Price = Double.parseDouble(fields[2].trim());
        String ISBN = fields[3].trim();
        String Genre_Code = fields[4].trim();
        int yr = Integer.parseInt(fields[5].trim());
        return new Book(Title, Author, Price, ISBN, yr, Genre_Code);
    }

    /**
     * Extracts fields from a CSV line.
     * @param line The input CSV line.
     * @return An array containing the extracted fields.
     */
    public static String[] extractField(final String line) {
        String s = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
        String[] split_field = line.split(s, -1);

        // Extracting individual fields
        String s1 = "^\"|\"$";
        split_field[0] = split_field[0].replaceAll(s1, "");

        return split_field;
    }

    /**
     * Validates syntax of CSV files.
     * @param filecount The number of files to validate.
     * @param reader The BufferedReader object for reading input file names.
     * @param fileMap A map storing file names and associated PrintWriter objects.
     * @throws IOException If an I/O error occurs.
     */
    static void validatesyntax(final int filecount, final BufferedReader reader, final UserDefinedMaps fileMap)
            throws IOException {
        for (int i = 0; i < filecount; i++) {
            String filename = reader.readLine();
            final File file = new File(filename);
            if (!file.exists()) {
                System.out.println("File not found: " + filename);
                continue;
            }
            validatesyntax(file, fileMap);
        }
    }

    /**
     * Validates syntax of a specific CSV file.
     * @param file The CSV file to validate.
     * @param fileMap A map storing file names and associated PrintWriter objects.
     */
    private static void validatesyntax(File file, UserDefinedMaps fileMap) {
        final File syntaxErrorF = new File("syntax_errors.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] field = Util.extractField(line);
                try {
                    validatesyntax(field);

                    // Write to output file
                    final String Genre_Code = field[4];
                    final PrintWriter writer = (PrintWriter) fileMap.getValue(Genre_Code);
                    writer.println(line);

                    // Update record count
                    final String fileName = BookBrowser.Genre_Codes_VS_CSV_OP_File_Name.getValue(Genre_Code).toString();
                    final int count = (int) BookBrowser.CSV_RECORD_Count.getValue(fileName);
                    BookBrowser.CSV_RECORD_Count.putValue(fileName, count + 1);
                } catch (final TooFewFieldsException | TooManyFieldsException | MissingFieldException
                        | UnknownGenreException e) {
                    logError(syntaxErrorF, file, e.getMessage(), line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error validating file : " + file.getName());
        }
    }

    /**
     * Validates the syntax of fields in a CSV line.
     * @param fields An array containing the fields of a CSV line.
     * @throws TooFewFieldsException If there are too few fields in the line.
     * @throws TooManyFieldsException If there are too many fields in the line.
     * @throws MissingFieldException If any field is missing in the line.
     * @throws UnknownGenreException If the genre code is unknown.
     */

    private static void validatesyntax(final String[] fields)
            throws TooFewFieldsException, TooManyFieldsException, MissingFieldException, UnknownGenreException {

        // validation for each field
        validatefields(fields);

        // Validate genre
        validategenre(fields[4]);
    }

    /**
     * Validates semantic errors in the fields of a CSV line.
     * @param field An array containing the fields of a CSV line.
     * @throws BadIsbn10Exception If the ISBN-10 is invalid.
     * @throws BadIsbn13Exception If the ISBN-13 is invalid.
     * @throws BadPriceException If the price is invalid.
     * @throws BadYearException If the year is invalid.
     */
    static void validsemanticerrors(final String[] field)
            throws BadIsbn10Exception, BadIsbn13Exception, BadPriceException, BadYearException {

        // Validate price
        Double pr = Double.parseDouble(field[2]);
        validateprice(pr);

        // Validate ISBN
        validateISBN(field[3]);

        // Validate year
        Integer yr = Integer.parseInt(field[5]);
        validateyear(yr);

    }

    /**
     * Validates the number of fields, ensuring it's neither too few nor too many.
     * Also checks for missing fields.
     * @param field An array containing the fields of a CSV line.
     * @throws MissingFieldException If any field is missing in the line.
     * @throws TooFewFieldsException If there are too few fields in the line.
     * @throws TooManyFieldsException If there are too many fields in the line.
     */
    public static void validatefields(final String[] field)
            throws MissingFieldException, TooFewFieldsException, TooManyFieldsException {
        // Validate number of fields
        if (field.length < 6) {
            throw new TooFewFieldsException("too few fields");
        } else if (field.length > 6) {
            throw new TooManyFieldsException("too many fields");
        }

        // Validate each field
        if (field[0].isEmpty()) {
            throw new MissingFieldException("missing title");
        }
        if (field[1].isEmpty()) {
            throw new MissingFieldException("missing authors");
        }
        if (field[2].isEmpty()) {
            throw new MissingFieldException("missing price");
        }
        if (field[3].isEmpty()) {
            throw new MissingFieldException("missing isbn");
        }
        if (field[4].isEmpty()) {
            throw new MissingFieldException("missing genre");
        }
        if (field[5].isEmpty()) {
            throw new MissingFieldException("missing year");
        }
    }

    /**
     * Validates the genre code.
     * @param genreCode The genre code to validate.
     * @throws UnknownGenreException If the genre code is unknown.
     */
    public static void validategenre(String genreCode) throws UnknownGenreException {
        String Genre = null;
        for (String validGenre : BookBrowser.Genre_Codes) {
            if (genreCode.equals(validGenre)) {
                Genre = validGenre;
            }
        }
        if (Genre == null) {
            throw new UnknownGenreException("invalid genre");
        }
    }

    /**
     * Validates the price, ensuring it's non-negative.
     * @param pr The price to validate.
     * @throws BadPriceException If the price is negative.
     */
    public static void validateprice(final Double pr) throws BadPriceException {
        if (pr < 0) {
            throw new BadPriceException("invalid price");
        }
    }

    /**
     * Validates the ISBN-10 or ISBN-13.
     * @param ISBN The ISBN to validate.
     * @throws BadIsbn10Exception If the ISBN-10 is invalid.
     * @throws BadIsbn13Exception If the ISBN-13 is invalid.
     */
    static void validateISBN(String ISBN) throws BadIsbn10Exception, BadIsbn13Exception {

        if (containsNonDigitChar(ISBN) || (ISBN.length() != 10 && ISBN.length() != 13)) {
            throw new BadIsbn10Exception("Invalid ISBN");
        }

        int sum = 0;
        if (ISBN.length() == 10) {
            for (int i = 0; i < 9; i++) {
                sum += (10 - i) * Character.getNumericValue(ISBN.charAt(i));
            }
            char lastchar = ISBN.charAt(9);
            if (lastchar == 'X') {
                sum += 10;
            } else {
                sum += Character.getNumericValue(lastchar);
            }
            if (sum % 11 != 0) {
                throw new BadIsbn10Exception("Invalid ISBN-10");
            }
        } else {
            for (int i = 0; i < 13; i++) {
                sum += (i % 2 == 0) ? Character.getNumericValue(ISBN.charAt(i))
                        : 3 * Character.getNumericValue(ISBN.charAt(i));
            }
            if (sum % 10 != 0) {
                throw new BadIsbn13Exception("Invalid ISBN-13");
            }
        }
    }

    /**
     * Validates the publication year.
     * @param yr The year to validate.
     * @throws BadYearException If the year is not within the valid range.
     */
    public static void validateyear(final int yr) throws BadYearException {
        if (yr < 1995 || yr > 2024) {
            throw new BadYearException("Invalid year");
        }
    }

    /**
     * Checks if a string contains non-digit characters.
     * @param str The string to check.
     * @return True if the string contains non-digit characters, otherwise false.
     */
    public static boolean containsNonDigitChar(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copies a portion of the book array to a new array.
     * @param books The original array of books.
     * @param totalBooks The total number of books to copy.
     * @return A new array containing the copied books.
     */
    static Book[] transfer(final Book[] books, final int totalBooks) {
        final Book[] newbook = new Book[totalBooks];
        for (int i = 0; i < totalBooks; i++) {
            newbook[i] = books[i];
        }
        return newbook;
    }

    /**
     * Logs syntax errors to a file.
     * @param errorFile The file to log errors to.
     * @param file The file being processed.
     * @param error The error message.
     * @param line The line where the error occurred.
     */
    public static void logError(File errorFile, File file, final String error, final String line) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(errorFile, true))) {
            writer.write("syntax error in file: " + file.getName());
            writer.newLine();
            writer.write("====================");
            writer.newLine();
            writer.write("Error: " + error);
            writer.newLine();
            writer.write("Record: " + line);
            writer.newLine();
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes PrintWriter objects for writing to CSV files.
     * @param fileNames An array of CSV file names.
     * @return An array of PrintWriter objects.
     */
    static PrintWriter[] getWriter(final String[] fileNames) {
        final PrintWriter[] writers = new PrintWriter[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            try {
                writers[i] = new PrintWriter(new FileWriter(fileNames[i]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return writers;
    }

    /**
     * Serializes an array of Book objects to a binary file.
     * @param fileName The name of the binary file to serialize to.
     * @param books The array of Book objects to serialize.
     */
    static void serializeBook(String fileName, Book[] books) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName + ".ser"))) {
            // Write the array length to the file
            outputStream.writeInt(books.length);
            // Write the array of books to the file
            outputStream.writeObject(books);
        } catch (IOException e) {
            System.out.println("Error serializing books: " + fileName);
            e.printStackTrace();
        }
    }

    /**
     * Deserializes an array of Book objects from a binary file.
     * @param fileName The name of the binary file to deserialize from.
     * @return An array of Book objects deserialized from the file.
     */
    public static Book[] deserializebook(String fileName) {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName + ".ser"))) {
            // Read the array length from the file
            int length = inputStream.readInt();

            // Read the array of objects from the file
            Object[] objects = (Object[]) inputStream.readObject();
            Book[] books = new Book[length];
            for (int i = 0; i < length; i++) {
                books[i] = (Book) objects[i];
            }
            return books;
        } catch (EOFException e) {
            System.out.println("End of file reached while deserializing books: " + fileName);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error deserializing books: " + fileName);
            e.printStackTrace();
        }
        return new Book[0];
    }
}

/**
 * Exception class for when there are too many fields in a record
 */
class TooManyFieldsException extends Exception {
    public TooManyFieldsException(String message) {
        super(message);
    }
}

/**
 * Exception class for when there are too few fields in a record
 */

class TooFewFieldsException extends Exception {
    public TooFewFieldsException(String message) {
        super(message);
    }
}

/**
 * // Exception class for when a required field is missing in a record
 */
class MissingFieldException extends Exception {
    public MissingFieldException(String message) {
        super(message);
    }
}

/**
 * // Exception class for when an unknown genre is encountered
 */
class UnknownGenreException extends Exception {
    public UnknownGenreException(String message) {
        super(message);
    }
}

/**
 * // Exception class for when there's an invalid ISBN-10 format
 */
class BadIsbn10Exception extends Exception {
    public BadIsbn10Exception(String message) {
        super(message);
    }
}

/**
 * // Exception class for when there's an invalid ISBN-13 format
 */
class BadIsbn13Exception extends Exception {
    public BadIsbn13Exception(String message) {
        super(message);
    }
}

/**
 * // Exception class for when there's an invalid price value
 */
class BadPriceException extends Exception {
    public BadPriceException(String message) {
        super(message);
    }
}

/**
 * // Exception class for when there's an invalid year value
 */
class BadYearException extends Exception {
    public BadYearException(String message) {
        super(message);
    }
}

class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String Title;
    private final String Author;
    private final double Price;
    private final String ISBN;
    private final int year;
    private final String Genre_Code;

    public Book(String title, String Author, double price, String ISBN, int year, String Genre_Code) {
        this.Title = title;
        this.Author = Author;
        this.Price = price;
        this.ISBN = ISBN;
        this.year = year;
        this.Genre_Code = Genre_Code;
    }

    public String getTitle() {
        return Title;
    }

    public String getAuthor() {
        return Author;
    }

    public double getPrice() {
        return Price;
    }

    public String getISBN() {
        return ISBN;
    }

    public int getYear() {
        return year;
    }

    public String getGenre_Code() {
        return Genre_Code;
    }

    @Override
    public String toString() {
        return "Book{" +
                "title='" + Title + '\'' +
                ", authors='" + Author + '\'' +
                ", price=" + Price +
                ", isbn='" + ISBN + '\'' +
                ", genre='" + Genre_Code + '\'' +
                ", year=" + year +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Book book = (Book) obj;
        return Double.compare(book.Price, Price) == 0 &&
                year == book.year &&
                Title.equals(book.Title) &&
                Author.equals(book.Author) &&
                ISBN.equals(book.ISBN) &&
                Genre_Code.equals(book.Genre_Code);
    }
}

/**
 * // Custom class for a simple key-value mapping implementation
 */
class UserDefinedMaps {
    private Object[] keys;
    private Object[] values;
    private int size;

    public UserDefinedMaps(int capacity) {
        keys = new Object[capacity];
        values = new Object[capacity];
        size = 0;
    }

    public UserDefinedMaps(Object[] keys, Object[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException("Keys and values arrays must have the same length");
        }
        this.keys = keys;
        this.values = values;
        this.size = keys.length;
    }

    // Method to put a key-value pair into the mapping
    public void putValue(Object key, Object value) {
        for (int i = 0; i < size; i++) {
            if (keys[i].equals(key)) {
                values[i] = value;
                return;
            }
        }
        keys[size] = key;
        values[size] = value;
        size++;
    }

    // Method to get the value corresponding to a given key

    public Object getValue(Object key) {
        for (int i = 0; i < size; i++) {
            if (keys[i].equals(key)) {
                return values[i];
            }
        }
        return null;
    }
}
