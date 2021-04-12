package com.gmail.nossr50.database;

import com.gmail.nossr50.TestUtil;
import com.gmail.nossr50.datatypes.database.DatabaseType;
import com.google.common.io.Files;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static org.junit.Assert.*;


@RunWith(PowerMockRunner.class)
public class FlatFileDatabaseManagerTest {

    public static final @NotNull String TEST_FILE_NAME = "test.mcmmo.users";
    public static final int HEALTHY_RETURN_CODE = 0;
    public static final String BAD_FILE_LINE_ONE = "mrfloris:2420:::0:2452:0:1983:1937:1790:3042:1138:3102:2408:3411:0:0:0:0:0:0:0:0::642:0:1617583171:0:1617165043:0:1617583004:1617563189:1616785408::2184:0:0:1617852413:HEARTS:415:0:631e3896-da2a-4077-974b-d047859d76bc:5:1600906906:";
    public static final String BAD_DATA_FILE_LINE_TWENTY_THREE = "nossr51:baddata:::baddata:baddata:640:baddata:1000:1000:1000:baddata:baddata:baddata:baddata:16:0:500:20273:0:0:0:0::1000:0:0:baddata:1593543012:0:0:0:0::1000:0:0:baddata:IGNORED:1000:0:588fe472-1c82-4c4e-9aa1-7eefccb277e3:1:0:";
    private static File tempDir;
    private final static @NotNull Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final long PURGE_TIME = 2630000000L;
    private static @Nullable FlatFileDatabaseManager db;

    @Before
    public void init() {
        assertNull(db);
        tempDir = Files.createTempDir();
        db = new FlatFileDatabaseManager(new File(tempDir.getPath() + File.separator + TEST_FILE_NAME), logger, PURGE_TIME, 0, true);
    }

    @After
    public void tearDown() {
        TestUtil.recursiveDelete(tempDir);
        db = null;
    }

    //Nothing wrong with this database
    private static final String[] normalDatabaseData = {
            "nossr50:1000:::0:1000:640:1000:1000:1000:1000:1000:1000:1000:1000:16:0:500:0:0:0:0:0::1000:0:0:0:1593543012:0:0:0:0::1000:0:0:1593806053:HEARTS:1000:0:588fe472-1c82-4c4e-9aa1-7eefccb277e3:0:0:",
            "mrfloris:2420:::0:2452:0:1983:1937:1790:3042:1138:3102:2408:3411:0:0:0:0:0:0:0:0::642:0:1617583171:0:1617165043:0:1617583004:1617563189:1616785408::2184:0:0:1617852413:HEARTS:415:0:631e3896-da2a-4077-974b-d047859d76bc:5:1600906906:",
            "powerless:0:::0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0::0:0:0:0:0:0:0:0:0::0:0:0:0:HEARTS:0:0:e0d07db8-f7e8-43c7-9ded-864dfc6f3b7c:5:1600906906:"
    };

    private static final String[] badUUIDDatabaseData = {
            "nossr50:1000:::0:1000:640:1000:1000:1000:1000:1000:1000:1000:1000:16:0:500:0:0:0:0:0::1000:0:0:0:1593543012:0:0:0:0::1000:0:0:1593806053:HEARTS:1000:0:588fe472-1c82-4c4e-9aa1-7eefccb277e3:0:0:",
            "z750:2420:::0:2452:0:1983:1937:1790:3042:1138:3102:2408:3411:0:0:0:0:0:0:0:0::642:0:1617583171:0:1617165043:0:1617583004:1617563189:1616785408::2184:0:0:1617852413:HEARTS:415:0:3:5:1600906906:", //This one has an incorrect UUID representation
            "powerless:0:::0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0::0:0:0:0:0:0:0:0:0::0:0:0:0:HEARTS:0:0:e0d07db8-f7e8-43c7-9ded-864dfc6f3b7c:5:1600906906:"
    };

    private static final String[] outdatedDatabaseData = {
            "nossr50:1000:::0:1000:640:1000:1000:1000:1000:1000:1000:1000:1000:16:0:500:0:0:0:0:0::1000:0:0:0:1593543012:0:0:0:0::1000:0:0:1593806053:HEARTS:1000:0:588fe472-1c82-4c4e-9aa1-7eefccb277e3:0:0:",
            "mrfloris:2420:::0:2452:0:1983:1937:1790:3042:1138:3102:2408:3411:0:0:0:0:0:0:0:0::642:0:1617583171:0:1617165043:0:1617583004:1617563189:1616785408::2184:0:0:1617852413:HEARTS:415:0:631e3896-da2a-4077-974b-d047859d76bc:5:1600906906:",
            "electronicboy:0:::0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0::0:0:0:0:0:0:0:0:0::0:0:0:0:HEARTS:0:0:e0d07db8-f7e8-43c7-9ded-864dfc6f3b7c:" //This user is missing data added after UUID index
    };

    private static final String[] emptyLineDatabaseData = {
            "nossr50:1000:::0:1000:640:1000:1000:1000:1000:1000:1000:1000:1000:16:0:500:0:0:0:0:0::1000:0:0:0:1593543012:0:0:0:0::1000:0:0:1593806053:HEARTS:1000:0:588fe472-1c82-4c4e-9aa1-7eefccb277e3:0:0:",
            "mrfloris:2420:::0:2452:0:1983:1937:1790:3042:1138:3102:2408:3411:0:0:0:0:0:0:0:0::642:0:1617583171:0:1617165043:0:1617583004:1617563189:1616785408::2184:0:0:1617852413:HEARTS:415:0:631e3896-da2a-4077-974b-d047859d76bc:5:1600906906:",
            "kashike:0:::0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0::0:0:0:0:0:0:0:0:0::0:0:0:0:HEARTS:0:0:e0d07db8-f7e8-43c7-9ded-864dfc6f3b7c:5:1600906906:",
            "" //EMPTY LINE
    };

    private static final String[] emptyNameDatabaseData = {
            ":1000:::0:1000:640:1000:1000:1000:1000:1000:1000:1000:1000:16:0:500:0:0:0:0:0::1000:0:0:0:1593543012:0:0:0:0::1000:0:0:1593806053:HEARTS:1000:0:588fe472-1c82-4c4e-9aa1-7eefccb277e3:0:0:",
            "mrfloris:2420:::0:2452:0:1983:1937:1790:3042:1138:3102:2408:3411:0:0:0:0:0:0:0:0::642:0:1617583171:0:1617165043:0:1617583004:1617563189:1616785408::2184:0:0:1617852413:HEARTS:415:0:631e3896-da2a-4077-974b-d047859d76bc:5:1600906906:",
            "aikar:0:::0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0:0::0:0:0:0:0:0:0:0:0::0:0:0:0:HEARTS:0:0:e0d07db8-f7e8-43c7-9ded-864dfc6f3b7c:5:1600906906:"
    };

    private static final String[] duplicateNameDatabaseData = {
            "mochi:1000:::0:1000:640:1000:1000:1000:1000:1000:1000:1000:1000:16:0:500:0:0:0:0:0::1000:0:0:0:1593543012:0:0:0:0::1000:0:0:1593806053:HEARTS:1000:0:588fe472-1c82-4c4e-9aa1-7eefccb277e3:0:0:",
            "mochi:1000:::0:1000:640:1000:1000:1000:1000:1000:1000:1000:1000:16:0:500:0:0:0:0:0::1000:0:0:0:1593543012:0:0:0:0::1000:0:0:1593806053:HEARTS:1000:0:631e3896-da2a-4077-974b-d047859d76bc:0:0:",
    };

    private static final String[] duplicateUUIDDatabaseData = {
            "nossr50:1000:::0:1000:640:1000:1000:1000:1000:1000:1000:1000:1000:16:0:500:0:0:0:0:0::1000:0:0:0:1593543012:0:0:0:0::1000:0:0:1593806053:HEARTS:1000:0:588fe472-1c82-4c4e-9aa1-7eefccb277e3:0:0:",
            "mrfloris:1000:::0:1000:640:1000:1000:1000:1000:1000:1000:1000:1000:16:0:500:0:0:0:0:0::1000:0:0:0:1593543012:0:0:0:0::1000:0:0:1593806053:HEARTS:1000:0:588fe472-1c82-4c4e-9aa1-7eefccb277e3:0:0:",
    };

    private static final String[] corruptDatabaseData = {
            "nossr50:1000:::0:100:0:0::1000:0:0:1593806053:HEARTS:1000:0:588fe472-1c82-4c4e-9aa1-7eefccb277e3:0:0:",
            "mrfloris:2420:::0:2452:0:1983:1937:1790:3042:1138:3102:2408:3411:0:0:0:0:0:0:0:0::642:0:1617583171:0:1617165043:0:1617583004:1617563189:1616785408::2184:0:0:1617852413:HEARTS:415:0:631e3896-da2a-4077-974b-d047859d76bc:5:1600906906:",
            "corruptdataboy:の:::ののの0:2452:0:1983:1937:1790:3042ののののの:1138:3102:2408:3411:0:0:0:0:0:0:0:0::642:0:1617のののののの583171:0:1617165043:0:1617583004:1617563189:1616785408::2184:0:0:1617852413:HEARTS:415:0:d20c6e8d-5615-4284-b8d1-e20b92011530:5:1600906906:",
            "のjapaneseuserの:333:::0:2452:0:444:1937:1790:3042:1138:3102:2408:3411:0:0:0:0:0:0:0:0::642:0:1617583171:0:1617165043:0:1617583004:1617563189:1616785408::2184:0:0:1617852413:HEARTS:415:0:25870f0e-7558-4659-9f60-417e24cb3332:5:1600906906:",
            "sameUUIDasjapaneseuser:333:::0:442:0:544:1937:1790:3042:1138:3102:2408:3411:0:0:0:0:0:0:0:0::642:0:1617583171:0:1617165043:0:1617583004:1617563189:1616785408::2184:0:0:1617852413:HEARTS:415:0:25870f0e-7558-4659-9f60-417e24cb3332:5:1600906906:",
    };

    private static final String[] badDatabaseData = {
            //First entry here is missing some values
            "nossr50:1000:0:500:0:0:0:0:0::1000:0:0:0:1593543012:0:0:0:0::1000:0:0:1593806053:HEARTS:1000:0:588fe472-1c82-4c4e-9aa1-7eefccb277e3:0:0:",
            //Second entry here has an integer value replaced by a string
            "mrfloris:2420:::0:2452:0:1983:1937:1790:3042:badvalue:3102:2408:3411:0:0:0:0:0:0:0:0::642:0:1617583171:0:1617165043:0:1617583004:1617563189:1616785408::2184:0:0:1617852413:HEARTS:415:0:631e3896-da2a-4077-974b-d047859d76bc:5:1600906906:"
    };

    @Test
    public void testPurgePowerlessUsers() {
        replaceDataInFile(db, normalDatabaseData);
        int purgeCount = db.purgePowerlessUsers();
        assertEquals(purgeCount, 1); //1 User should have been purged
    }

    @Test
    public void testCheckFileHealthAndStructure() {
        replaceDataInFile(db, badDatabaseData);

        List<FlatFileDataFlag> dataFlags = db.checkFileHealthAndStructure();
        assertNotNull(dataFlags);
        assertNotEquals(dataFlags.size(), 0);
    }

    @Test
    public void testFindFixableDuplicateNames() {
        overwriteDataAndCheckForFlag(db, duplicateNameDatabaseData, FlatFileDataFlag.DUPLICATE_NAME);
    }

    @Test
    public void testFindDuplicateUUIDs() {
        overwriteDataAndCheckForFlag(db, duplicateUUIDDatabaseData, FlatFileDataFlag.DUPLICATE_UUID);
    }

    @Test()
    public void findBadUUIDData() {
        overwriteDataAndCheckForFlag(db, badUUIDDatabaseData, FlatFileDataFlag.BAD_UUID_DATA);
    }

    @Test
    public void testFindCorruptData() {
        overwriteDataAndCheckForFlag(db, corruptDatabaseData, FlatFileDataFlag.CORRUPTED_OR_UNRECOGNIZABLE);
    }

    @Test
    public void testFindEmptyNames() {
        overwriteDataAndCheckForFlag(db, emptyNameDatabaseData, FlatFileDataFlag.MISSING_NAME);
    }

    @Test
    public void testFindBadValues() {
        overwriteDataAndCheckForFlag(db, badDatabaseData, FlatFileDataFlag.BAD_VALUES);
    }

    @Test
    public void testFindOutdatedData() {
        overwriteDataAndCheckForFlag(db, outdatedDatabaseData, FlatFileDataFlag.INCOMPLETE);
    }

    @Test
    public void testGetDatabaseType() {
        assertNotNull(db);
        assertEquals(db.getDatabaseType(), DatabaseType.FLATFILE);
    }

    @Test
    public void testLoadFromFile() {
        Path resourceDirectory = Paths.get("src","test","resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();

        ClassLoader classLoader = getClass().getClassLoader();
        URI resourceFileURI = null;

        try {
            resourceFileURI = classLoader.getResource("baddatadb.users").toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        assertNotNull(resourceFileURI);
        File fromResourcesFile = new File(resourceFileURI);
        assertNotNull(resourceFileURI);
        File copyOfFile = new File(tempDir.getPath() + File.separator + "baddatafile.users");

        if(copyOfFile.exists()) {
            copyOfFile.delete();
        }

        assertTrue(fromResourcesFile.exists());

        try {
            Files.copy(fromResourcesFile, copyOfFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertNotNull(copyOfFile);

        //This makes sure our private method is working before the tests run afterwards
        ArrayList<String[]> dataFromFile = getSplitDataFromFile(copyOfFile);
        System.out.println("File Path: "+copyOfFile.getAbsolutePath());
        assertEquals(BAD_FILE_LINE_ONE.split(":"), dataFromFile.get(0));
        assertEquals(dataFromFile.get(22)[0], "nossr51");
        assertEquals(BAD_DATA_FILE_LINE_TWENTY_THREE.split(":"), dataFromFile.get(22));

        FlatFileDatabaseManager db_a = new FlatFileDatabaseManager(copyOfFile, logger, PURGE_TIME, 0, true);
        List<FlatFileDataFlag> flagsFound = db_a.checkFileHealthAndStructure();
        assertNotNull(flagsFound);
        assertTrue(flagsFound.contains(FlatFileDataFlag.BAD_VALUES));
    }

    private @NotNull ArrayList<String[]> getSplitDataFromFile(@NotNull File file) {
        ArrayList<String[]> splitDataList = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if (line.isEmpty())
                    continue;

                String[] splitData = line.split(":");
                splitDataList.add(splitData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return splitDataList;
    }

    private void replaceDataInFile(@NotNull FlatFileDatabaseManager flatFileDatabaseManager, @NotNull String[] dataEntries) {
        String filePath = flatFileDatabaseManager.getUsersFile().getAbsolutePath();
        BufferedReader in = null;
        FileWriter out = null;

        try {
            StringBuilder writer = new StringBuilder();

            for(String data : dataEntries) {
                writer.append(data).append("\r\n");
            }

            out = new FileWriter(filePath);
            out.write(writer.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File not found");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException e) {
                    // Ignore
                }
            }
        }

        try {
            System.out.println("Added the following lines to the FlatFileDatabase for the purposes of the test...");
            // Open the file
            in = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    // Ignore
                }
            }
        }

    }

    private void overwriteDataAndCheckForFlag(@NotNull FlatFileDatabaseManager targetDatabase, @NotNull String[] data, @NotNull FlatFileDataFlag flag) {
        replaceDataInFile(targetDatabase, data);

        List<FlatFileDataFlag> dataFlags = targetDatabase.checkFileHealthAndStructure();
        assertNotNull(dataFlags);
        assertTrue(dataFlags.contains(flag));
    }
}