package nxcs.testbed;

import com.rits.cloning.Cloner;
import nxcs.*;
import nxcs.stats.Snapshot;
import nxcs.stats.StatsLogger;
import nxcs.stats.StepSnapshot;
import nxcs.stats.StepStatsLogger;

import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

//import nxcs.Trace;

/**
 * Represents a maze problem which is loaded in from a file. Such a file should
 * contain a rectangular array of 'O', 'T' and 'F' characters. A sample is given
 * in Mazes/Woods1.txt
 */
public class maze1_weighted_sum implements Environment {
    private final static List<Snapshot> stats = new ArrayList<Snapshot>();
    public static List<Integer> act = new ArrayList<Integer>();
    /**
     * The current position of the agent in the maze
     */
    public int x, y;
    /**
     * The raw characters in the maze
     */
    private char[][] mazeTiles;
    /**
     * The map from characters to their binary encodings used in states and
     * conditions
     */
    private Map<Character, String> encodingTable;
    /**
     * A list of points representing locations we can safely move the agent to
     */
    private List<Point> openLocations;
    /**
     * A list of points representing the final states in the environment
     */
    public List<Point> finalStates;
    private ArrayList<Reward> rewardGrid;
    /**
     * A list which maps the indices to (delta x, delta y) pairs for moving the
     * agent around the environment
     */
    private List<Point> actions;
    /**
     * The number of timesteps since the agent last discovered a final state
     */
    private int count;


    private Cloner cloner;

    /**
     * Loads a maze from the given maze file
     *
     * @param mazeFile The filename of the maze to load
     * @throws IOException On standard IO problems
     */
    public maze1_weighted_sum(String mazeFile) throws IOException {
        this(new File(mazeFile));
    }

    /**
     * Loads a maze from the given maze file
     *
     * @param f The file of the maze to load
     * @throws IOException On standard IO problems
     */
    public maze1_weighted_sum(File f) throws IOException {
        // Set up the encoding table FOR DST
        encodingTable = new HashMap<Character, String>();
        encodingTable.put('O', "000");
        encodingTable.put('T', "110");
        encodingTable.put(null, "100");// For out of the maze positions
        encodingTable.put('F', "111");
        encodingTable.put('N', "011");

        // encodingTable.put('1', "001");
        // encodingTable.put('3', "011");
        // encodingTable.put('5', "101");
        // encodingTable.put('8', "010");

        openLocations = new ArrayList<Point>();
        finalStates = new ArrayList<Point>();
        rewardGrid = new ArrayList<Reward>();

        actions = new ArrayList<Point>();
        actions.add(new Point(0, -1));// Up
        actions.add(new Point(-1, 0));// Left
        actions.add(new Point(1, 0));// Right
        actions.add(new Point(0, 1));// Down
        // actions.add(new Point(-1, -1));// Up, Left
        // actions.add(new Point(1, -1));// Up, Right
        // actions.add(new Point(-1, 1));// Down, Left
        // actions.add(new Point(1, 1));// Down, Right

        // Load the maze into a char array
        List<String> mazeLines = new ArrayList<String>();
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = reader.readLine()) != null) {
                mazeLines.add(line);
            }
        }
        mazeTiles = new char[mazeLines.size()][];
        for (int i = 0; i < mazeLines.size(); i++) {
            mazeTiles[i] = mazeLines.get(i).toCharArray();
            if (i > 0 && mazeTiles[i].length != mazeTiles[1].length) {
                throw new IllegalArgumentException(
                        String.format("Line %d in file %s is of different length than the others", i + 1, f.getName()));
            }

            for (int j = 0; j < mazeTiles[i].length; j++) {
                char c = mazeTiles[i][j];
                if (!encodingTable.containsKey(c)) {
                    throw new IllegalArgumentException(
                            String.format("Line %d in file %s has an invalid character %c", i + 1, f.getName(), c));
                }

                if (c == 'O') {
                    openLocations.add(new Point(j, i));
                    rewardGrid.add(new Reward(new Point(j, i), -1, 0));
//				} else if (c != 'O' && c != 'T') {
                } else if (c == 'F') {
                    finalStates.add(new Point(j, i));
                    rewardGrid.add(new Reward(new Point(j, i), -1, Character.getNumericValue(c)));
                }
            }

        }

        System.out.println("openLocations:" + openLocations);
        System.out.println("finalStates:" + finalStates);
        System.out.println("rewardGrid:" + rewardGrid);
    }

    public static void main(String[] args) throws IOException {

        Map<Integer, Map<Integer, Double>> tempList = new HashMap<Integer, Map<Integer, Double>>();
        BufferedWriter writer = null;

        // create a temporary file
        String timeLog = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());// yyyyMMdd_HHmmss
        File logFile = new File(timeLog);

        SimpleDateFormat dateformatyyyyMMdd = new SimpleDateFormat("yyyyMMddHHmm");
        String fileTimestamp = dateformatyyyyMMdd.format(new Date());

        writer = new BufferedWriter(new FileWriter(logFile));

        int totalCalcTimes = 1;
        int finalStateUpperBound = 2335;
        int repeat_time = 1;

        act.add(0);
        act.add(1);
        act.add(2);
        act.add(3);

        try {

			/*
            * TODO: explore 1 weight, but different reward first
			* Loop:weight
			*    Loop:diff final reward for obj1
			*
			* */
//			double[][] weights = new double[][] {{ 1, 1 }, { 0, 1 }, { 1, 0 } };


            NXCSParameters params = new NXCSParameters();
            // Another set of parameters Woods1, Woods101

            params.N = 3000;
            params.stateLength = 24;
            params.numActions = 4;
            params.rho0 = 1000;
            params.pHash = 0.1;
            params.gamma = 0.5;
            params.crossoverRate = 0.8;
            params.mutationRate = 0.04;
            params.thetaMNA = 4;
            params.thetaGA = 500;
            // params.thetaGA = 0;
            // params.e0 = 0.05;
            params.e0 = 0.05;
            params.thetaDel = 200;
            params.doActionSetSubsumption = false;
            params.doGASubsumption = false;
            params.weights = new ArrayList<Point>();
            params.weights.add(new Point(0, 10));
//            params.weights.add(new Point(1, 9));
//            params.weights.add(new Point(2, 8));
//            params.weights.add(new Point(3, 7));
//            params.weights.add(new Point(4, 6));
//            params.weights.add(new Point(5, 5));
//            params.weights.add(new Point(6, 4));
//            params.weights.add(new Point(7, 3));
//            params.weights.add(new Point(8, 2));
//            params.weights.add(new Point(9, 1));
            params.weights.add(new Point(10, 0));

//            params.obj1 = new int[]{10, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
            params.obj1 = new int[]{100};

            ArrayList<Point> reward_CSV = new ArrayList<Point>();
            for (int i = 0; i < params.obj1.length; i++) {
                reward_CSV.add(new Point(params.obj1[i], 1000 - params.obj1[i]));
            }

            maze1_weighted_sum maze = new maze1_weighted_sum("data/maze5.txt");


            int finalStateCount = 1;
            boolean logged = false;
            int resultInterval = 2300;
            int numOfChartBars = 20;
            // ArrayList<Point> traceWeights = new ArrayList<Point>();
            // traceWeights.add(new Point(10, 90));
            // traceWeights.add(new Point(95, 5));

            // picture: finalStateUpperBound / 20) / 10 * 10 should be 20
            int chartXInterval = ((finalStateUpperBound / numOfChartBars) > 10)
                    ? (finalStateUpperBound / numOfChartBars) / 10 * 10 : 10;

            //Loop weights
            for (Point weight : params.weights) {

                //Loop:diff final reward for obj1
                for (int obj_num = 0; obj_num < params.obj1.length; obj_num++) {

                    //set reward for each round
                    double first_Freward = params.obj1[obj_num];
                    double second_Freward = 1000 - params.obj1[obj_num];

                    for (int repeat = 0; repeat < repeat_time; repeat++) {
                        //set logger
                        StepStatsLogger stepTrailsLogger = new StepStatsLogger(chartXInterval, 0);

                        StatsLogger crossTrialStats = new StatsLogger(chartXInterval, 0);

                        //totalCalcTimes:how many runs want to avg, here set 1 to ignor this loop
                        for (int trailIndex = 0; trailIndex < totalCalcTimes; trailIndex++) {
                            maze = new maze1_weighted_sum("data/maze6.txt");
                            NXCS nxcs = new NXCS(maze, params);

                            int stepi = 1;
                            maze.resetPosition();

                            finalStateCount = 1;

                            // clear stats
                            stats.clear();

                            StepStatsLogger stepLogger = new StepStatsLogger(chartXInterval, 0);
                            StepStatsLogger stepLogger_test = new StepStatsLogger(chartXInterval, 0);

                            System.out.println(
                                    String.format("######### begin to run of: Weight:%s - first reward:%s - Trail#: %s ",
                                            weight, params.obj1[obj_num], trailIndex));


                            while (finalStateCount < finalStateUpperBound) {

//                                System.out.println("next step:" + stepi);

                                nxcs.runIteration(finalStateCount, maze.getState(), weight, stepi, params.obj1[obj_num]);

                                stepi++;


//                                if (finalStateCount > 2333) {
//                                    System.out.println("print classifiers at finalstatecount: " + finalStateCount);
//                                    maze.printOpenLocationClassifiers(finalStateCount, maze, nxcs, weight, params.obj1[obj_num]);
//                                }

                                if (maze.isEndOfProblem(maze.getState())) {
                                    maze.resetPosition();
//								System.out.println("goalstate*************************");
//								maze.resetToSamePosition(new Point(2, 1));
                                    finalStateCount++;
                                    logged = false;
                                    // System.out.println(finalStateCount);
                                }

                                // analyst results
                                // if (((finalStateCount % resultInterval ==
                                // 0)||(finalStateCount<100)) && !logged) {
                                if ((finalStateCount % resultInterval == 0) && !logged) {
                                    // test algorithem
                                    System.out.println("testing process: Trained on " + finalStateCount + " final states");

                                    Integer test = 0;
                                    int total_timestamp = 0;
                                    int timestamp = 0;
                                    System.out.println("strat testing:");


                                    //testing process for 4 open states from (2,1)
                                    int[] ActionSelect = new int[maze.openLocations.size()];
                                    int logFlag = 0;
                                    int resetPoint = 0;

                                    maze.resetToSamePosition(maze.openLocations.get(test));
                                    while (test < maze.openLocations.size()) {

                                        String state = maze.getState();

                                        System.out.println(String.format("@1 Test:%d, Steps:%d, state:%s", resetPoint, logFlag, maze.getxy()));
                                        int action = nxcs.classify(state, weight);

                                        if (logFlag == 0) {
                                            ActionSelect[resetPoint] = action;
                                        }
                                        // System.out.println("choose action");
                                        logFlag++;
                                        //TODO:return the PA1[action]
//                                        System.out.println(String.format("@2 Timestamp:%d, test:%d, resetPoint:%d, logFlag:%d, state:%s", timestamp, test, resetPoint, logFlag, maze.getxy()));

                                        double selectedPA_reward = nxcs.getSelectPA(action, state);

                                        ActionPareto r = maze.getReward(state, action, first_Freward);
//                                        if (r.getAction() == 5) {
//                                            System.out.println("get stucked:classfiers:");
//
//                                            maze.printOpenLocationClassifiers(finalStateCount, maze, nxcs, weight, params.obj1[obj_num]);
//                                        }
                                        // System.out.println("take testing:");
                                        if (maze.isEndOfProblem(maze.getState())) {
                                            System.out.println(String.format("@3 Test:%d, Steps:%d, state:%s", resetPoint, logFlag, maze.getxy()));
                                            test++;
                                            if (test < maze.openLocations.size()) {
                                                Point testPoint = maze.openLocations.get(test); //maze.getTestLocation(test, testLocations);
                                                resetPoint++;

                                                maze.resetToSamePosition(testPoint);
                                                System.out.println(String.format("Reset to Test:%d, resetPoint:%d, testLocation:%s", test, test, testPoint));
                                                logFlag = 0;
                                            }
                                        }
                                        total_timestamp++;
                                    }

                                    //TODO:write first_selected_PA in CSV
                                    /*************
                                     * stepLogger for testing
                                     * stepLogger.add(maze.traceOpenLocations(finalStateCount, maze, trace, nxcs, params));
                                     *****/
                                    ArrayList<StepSnapshot> testStats = new ArrayList<StepSnapshot>();


                                    testStats.addAll(maze.GetTestingPAResultInCSV(repeat, finalStateCount, maze, nxcs, weight, first_Freward, ActionSelect));

                                    System.out.println("log test info");
                                    stepLogger_test.add(testStats);


                                    finalStateCount++;

                                    System.out.println("avg steps:" + ((double) (total_timestamp)) / test);

                                    System.out.println("classfiers:");

                                    //TODO:print reward_l(PA[1]),reward_r(PA[2]),deltaReward and maxReward
                                    maze.printOpenLocationClassifiers(finalStateCount, maze, nxcs, weight, params.obj1[obj_num]);


                                    /*************
                                     * stepLogger for training
                                     * stepLogger.add(maze.traceOpenLocations(finalStateCount, maze, trace, nxcs, params));
                                     *****/
                                    ArrayList<StepSnapshot> trailStats = new ArrayList<StepSnapshot>();


                                    trailStats.addAll(maze.GetTrainingPAResultInCSV(repeat, finalStateCount, maze, nxcs, weight, first_Freward));
                                    System.out.println("log training info");
                                    stepLogger.add(trailStats);
                                    logged = true;

                                }

                                // run function below every 50 steps

                                // endof while

                                // System.out.println("Trained on " +
                                // finalStateCount + " final states - " +
                                // weight
                                // + " - " + distCalcMethod);

                            } // endof z loop

                            //write result to csv
                            stepLogger.writeLogAndCSVFiles(
//                                    String.format("log/maze1/csv/%s/%s/%s - %s - Trial %d - <TRIAL_NUM> - %d.csv", "MOXCS",
//                                    "MAZE4", weight, params.obj1[obj_num], trailIndex, params.N),
                                    String.format("log/maze1/csv/%s/%s - %s - Trial %d - TRIAL_NUM - %d.csv", "MOXCS",
                                            "Train", fileTimestamp, trailIndex, params.N),
                                    String.format("log/maze1/datadump/%s - %s - Trail %d-<TIMESTEP_NUM> - %d.log", "MOXCS",
                                            params.obj1[obj_num], trailIndex, params.N));
                            stepLogger_test.writeLogAndCSVFiles_TESTING(
                                    String.format("log/maze1/csv/%s/%s - %s - Trial %d - TRIAL_NUM - %d - TEST.csv", "MOXCS",
                                            "Train", fileTimestamp, trailIndex, params.N),
                                    String.format("log/maze1/datadump/%s - %s - Trail %d-<TIMESTEP_NUM> - %d.log", "MOXCS",
                                            params.obj1[obj_num], trailIndex, params.N));
                        } // calculator loop

                    }
                } // action selection loop

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        } // endof try
    }

    public List<Reward> getRewardGrid() {
        return rewardGrid;
    }

    /**
     * Resets the agent to a random open position in the environment
     */
    public void resetPosition() {
        Point randomOpenPoint = XienceMath.choice(openLocations);
        x = randomOpenPoint.x;
        y = randomOpenPoint.y;
        count = 0;
        // x = 1;
        // y = 1;
        // count = 0;
    }

    public void resetToSamePosition(Point xy) {
        x = xy.x;
        y = xy.y;
        count = 0;
        // x = 1;
        // y = 1;
        // count = 0;
    }


    /**
     * Returns the two-bit encoding for the given position in the maze
     *
     * @param x The x position in the maze to get
     * @param y The y position in the maze to get
     * @return The two-bit encoding of the given position
     */
    private String getEncoding(int x, int y) {
        if (x < 0 || y < 0 || y >= mazeTiles.length || x >= mazeTiles[0].length) {
            return encodingTable.get(null);
        } else {
            return encodingTable.get(mazeTiles[y][x]);

        }
    }

    /**
     * Calculates the 16-bit state for the given position, from the 8 positions
     * around it
     *
     * @param x The x position of the state to get the encoding for
     * @param y The y position of the state to get the encoding for
     * @return The binary representation of the given state
     */
    private String getStringForState(int x, int y) {
        StringBuilder build = new StringBuilder();
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0)
                    continue;
                build.append(getEncoding(x + dx, y + dy));
            }
        }
        return build.toString();
    }

    /**
     * Checks whether the given position is a valid position that the agent can
     * be in in this maze. A position is valid if it is inside the bounds of the
     * maze and is not a tree (T)
     *
     * @param x The x position to check
     * @param y The y position to check.
     * @return True if the given (x, y) position is a valid position in the maze
     */
    private boolean isValidPosition(int x, int y) {
        return !(x < 0 || y < 0 || y >= mazeTiles.length || x >= mazeTiles[0].length) && mazeTiles[y][x] != 'T' && mazeTiles[y][x] != 'N';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getState() {
//        System.out.println(String.format("x,y:%d %d", x, y));
        return getStringForState(x, y);
    }

    public Point getxy() {
        return new Point(x, y);
    }

    // public Qvector getReward(String state, int action) {
    // count++;
    // Qvector reward = new Qvector(-1, 0);
    //
    // Point movement = actions.get(action);
    // if (isValidPosition(x + movement.x, y + movement.y)) {
    // x += movement.x;
    // y += movement.y;
    // }
    //
    // if (x == 1 && y == 1) {
    // reward.setQvalue(-1, 1);
    // // resetPosition();
    // }
    //
    // if (x == 1 && y == 6) {
    // reward.setQvalue(-1, 10);
    // // resetPosition();
    // }
    //
    // if (count > 100) {
    // resetPosition();
    // action=-1;//???
    // reward.setQvalue(-1, 0);
    // }
    //
    // return reward;
    // }

    /**
     * {@inheritDoc}
     */
    @Override
    /* return reward and action */

    public ActionPareto getReward(String state, int action, double first_reward) {
        count++;
        ActionPareto reward = new ActionPareto(new Qvector(-1, 0), 1);

        try {
            Point movement = actions.get(action);
            if (isValidPosition(x + movement.x, y + movement.y)) {
                x += movement.x;
                y += movement.y;
            }

            if (x == finalStates.get(0).getX() && y == finalStates.get(0).getY()) {
                reward.setPareto(new Qvector(-1, first_reward));
                // resetPosition();
            }

            if (x == finalStates.get(1).getX() && y == finalStates.get(1).getY()) {
                reward.setPareto(new Qvector(-1, 1000 - first_reward));
                // resetPosition();
            }

            if (count > 100) {
//                System.out.println("count>100:");


//                printOpenLocationClassifiers(0, this, null, null, first_reward);


                resetPosition();
//				action = -1;
//                System.out.println("reset:" + "x:" + x + " y:" + y);
                reward.setPareto(new Qvector(-1, 0));// TODO:is that
                // correct??????
            }
        } catch (Exception e) {
            System.out.println(String.format("%s  %d", state, action));
            throw e;
        }

        return reward;
    }

    public boolean isEndOfProblem(String state) {
        for (Point finalState : finalStates) {
            if (getStringForState(finalState.x, finalState.y).equals(state)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<ArrayList<StepSnapshot>> traceReward(int exp_repeat, int timeStamp, maze1_weighted_sum maze, NXCS nxcs,
                                                           NXCSParameters params, Point weight, double first_reward) {
        // stats variables
        ArrayList<ArrayList<StepSnapshot>> locStats = new ArrayList<ArrayList<StepSnapshot>>();
        for (Point p : maze.openLocations) {
            maze.resetToSamePosition(p);
            String startState = maze.getState();
            ArrayList<StepSnapshot> trc = GetTrainingPAResultInCSV(exp_repeat, timeStamp, maze, nxcs, weight, first_reward);
            // ArrayList<StepSnapshot> trc = trace.traceStart(startState, nxcs);
            trc.stream().forEach(x -> x.setTimestamp(timeStamp));
            locStats.add(trc);
        }
        return locStats;
    }

    public void printOpenLocationClassifiers(int timestamp, maze1_weighted_sum maze, NXCS nxcs, Point weight, double obj_r1) {
        System.out.println("R1 is:" + obj_r1 + " R2 is:" + (1000 - obj_r1));

        for (Point p : maze.openLocations) {
            System.out.println(String.format("%d\t location:%d,%d", timestamp, (int) p.getX(), (int) p.getY()));


            List<Classifier> C = nxcs.generateMatchSet(maze.getStringForState(p.x, p.y));
            double[] PA1 = nxcs.generatePredictions(C, 0);
            for (int i = 0; i < PA1.length; i++) {
                System.out.println("PA1[" + i + "]:" + PA1[i]);
            }
            double[] PA2 = nxcs.generatePredictions(C, 1);
            for (int i = 0; i < PA2.length; i++) {
                System.out.println("PA2[" + i + "]:" + PA2[i]);
            }
            double[] PA = nxcs.generateTotalPredictions(C, weight);
            for (int i = 0; i < PA.length; i++) {
                System.out.println("PAt[" + i + "]:" + PA[i]);
            }

            //Q_finalreward
            double Q_finalreward_left = PA1[1];
            double Q_finalreward_right = PA1[2];
            double Q_finalreward_delta = PA1[1] - PA1[2];
            double Q_finalreward_max = 0;
            if (PA1[1] > PA1[2]) {
                Q_finalreward_max = PA1[1];
            } else {
                Q_finalreward_max = PA1[2];
            }

            //Q_steps
            double Q_steps_left = PA2[1];
            double Q_steps_right = PA2[2];
            double Q_steps_delta = PA2[1] - PA2[2];
            double Q_steps_min = 0;
            if (PA2[1] > PA2[2]) {
                Q_steps_min = PA2[2];
            } else {
                Q_steps_min = PA2[1];
            }

            //Q_weighted sum value for different weights
            //TODO:Q_weighted sum value for different weights


            for (int action : act) {

                List<Classifier> A = C.stream().filter(b -> b.action == action).collect(Collectors.toList());
                Collections.sort(A, new Comparator<Classifier>() {
                    @Override
                    public int compare(Classifier o1, Classifier o2) {
                        return o1.fitness == o2.fitness ? 0 : (o1.fitness > o2.fitness ? 1 : -1);
                    }
                });

                System.out.println("action:" + action);
                // TODO:
                // 1.why not print fitness of cl???????
                // 2.print PA for each state to see if PA correct
                System.out.println(A);

            }

        } // open locations
    }


    private ArrayList<StepSnapshot> GetTestingPAResultInCSV(int experiment_num, int timestamp, maze1_weighted_sum maze, NXCS nxcs, Point weight, double obj_r1, int[] ActionSelect) {

        ArrayList<StepSnapshot> PAresult = new ArrayList<StepSnapshot>();


        for (int p = 0; p < maze.openLocations.size(); p++) {

            Point point = new Point(p + 2, 1);

            List<Classifier> C = nxcs.generateMatchSet(maze.getStringForState(maze.openLocations.get(p).x, maze.openLocations.get(p).y));
            double[] PA1 = nxcs.generatePredictions(C, 0);

            double[] PA2 = nxcs.generatePredictions(C, 1);

            double[] PA1_nor = new double[4];
            double[] PA2_nor = new double[4];

            //normalisation
            for (int i = 0; i < PA1.length; i++) {
                PA1_nor[i] = nxcs.stepNor(PA1[i], 100);
            }
            for (int i = 0; i < PA2.length; i++) {
                PA2_nor[i] = nxcs.rewardNor(PA2[i], 1000, 0);
            }

            double[] PAt = nxcs.getTotalPrediciton(weight, PA1_nor, PA2_nor);

            //Q_finalreward
            double Q_finalreward_left = PA1[1];
            double Q_finalreward_right = PA1[2];
            double Q_finalreward_delta = PA1[1] - PA1[2];
            double Q_finalreward_max = 0;
            if (PA1[1] > PA1[2]) {
                Q_finalreward_max = PA1[1];
            } else {
                Q_finalreward_max = PA1[2];
            }


            //Q_steps
            double Q_steps_left = PA2[1];
            double Q_steps_right = PA2[2];
            double Q_steps_delta = PA2[1] - PA2[2];
            double Q_steps_min = 0;
            if (PA2[1] > PA2[2]) {
                Q_steps_min = PA2[2];
            } else {
                Q_steps_min = PA2[1];
            }

            double Q_total_left = PAt[1];
            double Q_total_right = PAt[2];


            double Q_finalreward_select = PA1[ActionSelect[p]];
            double Q_steps_select = PA2[ActionSelect[p]];
            double Q_total_select = PAt[ActionSelect[p]];

            //int exp_repeat, int finalCount, Point openState, double Q_finalreward_left, double Q_finalreward_right,double Q_finalreward_delta,double Q_finalreward_max, double Q_steps_left, double Q_steps_right,double Q_steps_delta,double Q_steps_max,ArrayList<Point> path) {

            StepSnapshot result_row = new StepSnapshot(experiment_num, timestamp, weight, obj_r1, point, Q_finalreward_left, Q_finalreward_right, Q_finalreward_delta, Q_finalreward_max, Q_steps_left, Q_steps_right, Q_steps_delta, Q_steps_min, Q_total_left, Q_total_right, Q_finalreward_select, Q_steps_select, Q_total_select);

            PAresult.add(result_row);
            //Q_weighted sum value for different weights
            //TODO:Q_weighted sum value for different weights


        } // open locations
        return PAresult;
    }

    private ArrayList<StepSnapshot> GetTrainingPAResultInCSV(int experiment_num, int timestamp, maze1_weighted_sum maze, NXCS nxcs, Point weight, double obj_r1) {

        ArrayList<StepSnapshot> PAresult = new ArrayList<StepSnapshot>();


        for (Point p : maze.openLocations) {

            List<Classifier> C = nxcs.generateMatchSet(maze.getStringForState(p.x, p.y));
            double[] PA1 = nxcs.generatePredictions(C, 0);

            double[] PA2 = nxcs.generatePredictions(C, 1);

            //Q_finalreward
            double Q_finalreward_left = PA1[1];
            double Q_finalreward_right = PA1[2];
            double Q_finalreward_delta = PA1[1] - PA1[2];
            double Q_finalreward_max = 0;
            if (PA1[1] > PA1[2]) {
                Q_finalreward_max = PA1[1];
            } else {
                Q_finalreward_max = PA1[2];
            }

            //Q_steps
            double Q_steps_left = PA2[1];
            double Q_steps_right = PA2[2];
            double Q_steps_delta = PA2[1] - PA2[2];
            double Q_steps_max = 0;
            if (PA2[1] > PA2[2]) {
                Q_steps_max = PA2[1];
            } else {
                Q_steps_max = PA2[2];
            }

            //int exp_repeat, int finalCount, Point openState, double Q_finalreward_left, double Q_finalreward_right,double Q_finalreward_delta,double Q_finalreward_max, double Q_steps_left, double Q_steps_right,double Q_steps_delta,double Q_steps_max,ArrayList<Point> path) {

            StepSnapshot result_row = new StepSnapshot(experiment_num, timestamp, weight, obj_r1, p, Q_finalreward_left, Q_finalreward_right, Q_finalreward_delta, Q_finalreward_max, Q_steps_left, Q_steps_right, Q_steps_delta, Q_steps_max);

            PAresult.add(result_row);
            //Q_weighted sum value for different weights
            //TODO:Q_weighted sum value for different weights


        } // open locations
        return PAresult;
    }


    public ArrayList<ArrayList<StepSnapshot>> getOpenLocationExpectPaths() {
        ArrayList<ArrayList<StepSnapshot>> expect = new ArrayList<ArrayList<StepSnapshot>>();
        ArrayList<StepSnapshot> e21 = new ArrayList<StepSnapshot>();
        e21.add(new StepSnapshot(new Point(2, 1), new Point(1, 1), 1));
        e21.add(new StepSnapshot(new Point(2, 1), new Point(6, 1), 4));
        expect.add(e21);
        ArrayList<StepSnapshot> e31 = new ArrayList<StepSnapshot>();
        e31.add(new StepSnapshot(new Point(3, 1), new Point(1, 1), 2));
        e31.add(new StepSnapshot(new Point(3, 1), new Point(6, 1), 3));
        expect.add(e31);
        ArrayList<StepSnapshot> e41 = new ArrayList<StepSnapshot>();
        e41.add(new StepSnapshot(new Point(4, 1), new Point(6, 1), 2));
        expect.add(e41);
        ArrayList<StepSnapshot> e51 = new ArrayList<StepSnapshot>();
        e51.add(new StepSnapshot(new Point(5, 1), new Point(6, 1), 1));
        expect.add(e51);

        return expect;
    }


    private HashMap<Integer, Point> getTestLocation() {
        HashMap<Integer, Point> ret = new HashMap<Integer, Point>();
        ret.put(0, new Point(2, 1));
        ret.put(1, new Point(3, 1));
        ret.put(2, new Point(4, 1));
        ret.put(3, new Point(5, 1));
        return ret;
    }

    private Point getTestLocation(Integer test, HashMap<Integer, Point> locations) {
        if (locations.containsKey(test))
            return locations.get(test);
        else
            return null;
    }
}
