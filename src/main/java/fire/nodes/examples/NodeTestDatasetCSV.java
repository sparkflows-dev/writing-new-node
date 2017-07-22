package fire.nodes.examples;

import fire.context.JobContext;
import fire.nodes.dataset.NodeDataset;
import fire.workflowengine.FireSchema;
import fire.workflowengine.Workflow;
import org.apache.spark.sql.Dataset; import org.apache.spark.sql.Row;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;

import java.io.Serializable;

/**
 * Created by jayantshekhar
 *
 * Creates a DataFrame from a CSV file
 */

public class NodeTestDatasetCSV extends NodeDataset implements Serializable {

    // field separator in the input file
    public String separator = ",";

    // does it have a header line
    public boolean header = true;

    // details of the output schema of this Node
    public String[] outputColNames = {};
    public FireSchema.Type[] outputColTypes   = {};
    public String[] outputColFormats   = {};

    //--------------------------------------------------------------------------------------

    public NodeTestDatasetCSV() {

    }

    public NodeTestDatasetCSV(int i, String nm, String path, String separator,
                          String cols, String ctypes, String cformats) {
        super(i, nm, path);

        this.separator = separator;

        FireSchema fireSchema = new FireSchema(cols, ctypes, cformats, null);
        this.outputColNames = fireSchema.colNames;
        this.outputColTypes = fireSchema.colTypes;
        this.outputColFormats = fireSchema.colFormats;
    }

    public NodeTestDatasetCSV(int i, String nm, String path, String separator,
                          String[] cols, FireSchema.Type[] colTypes) {
        super(i, nm, path);

        this.separator = separator;
        this.outputColNames = cols;
        this.outputColTypes = colTypes;
    }

    public NodeTestDatasetCSV(int i, String nm, String path) {
        super(i, nm, path);

    }
    //--------------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------------

    @Override
    public FireSchema getOutputSchema(Workflow workflow, FireSchema inputSchema) {

        FireSchema schema =  new FireSchema(outputColNames, outputColTypes, outputColFormats);
        schema.setColMLTypes();

        return schema;
    }

    //--------------------------------------------------------------------------------------
    @Override
    public void execute(JobContext jobContext) throws Exception {

        if (separator == null || separator.length() == 0)
            separator = ",";

        Dataset<Row> df = null;

        df = executeCSV(jobContext);

        // if it is running locally and synchronous, limit the number of rows processed
        if (jobContext.runningOn == JobContext.RUNNING_LOCALLY_SYNCHRONOUS) {
            df = df.limit(jobContext.numRowsForLocallySynchronous);
        }

        passDataFrameToNextNodesAndExecute(jobContext, df);
    }

    //------------------------------------------------------------------------------------------------------

    private Dataset<Row> executeCSV(JobContext jobContext) throws Exception {

        String headerstr = "true";
        if (header == false)
            headerstr = "false";

        FireSchema schema =  new FireSchema(outputColNames, outputColTypes, outputColFormats);
        schema.setColMLTypes();

        String dateFormat = schema.dateFormat();

        StructType customSchema = schema.toSparkSQLStructType();

        Dataset<Row> tdf = null;

        if (dateFormat.length() > 0) {
            tdf = jobContext.session().read()
                    .format("csv")
                    .option("header", headerstr) // Use first line of all files as header
                    .option("inferSchema", "false") // Automatically do not infer data types
                    .option("delimiter", separator)
                    .option("dateFormat", dateFormat)
                    .schema(customSchema)
                    .csv(path);

        } else {
            tdf = jobContext.session().read()
                    .format("csv")
                    .option("header", headerstr) // Use first line of all files as header
                    .option("inferSchema", "false") // Automatically do not infer data types
                    .option("delimiter", separator)
                    .schema(customSchema)
                    .csv(path);
        }

        return tdf;
    }


    //-------------------------------------------------------------------------------------------------------

}

