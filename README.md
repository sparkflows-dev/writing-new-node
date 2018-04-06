# Writing new Nodes/Processors for Fire

This repository contains a sample Node based on which new nodes can be created. This Node can be placed in the sparkflows web server and executed.

For more examples on nodes in Sparkflows, refer:

- https://github.com/sparkflows/sparkflows-stanfordcorenlp

# Spark 1.X / Spark 2.X

This repo has a master branch and spark-2.x.

- Master branch is for Spark 1.6.x
- spark-2.x is for Spark 2.1.x


## Directory Contents

Below is the contents of the directory.

* **fire-core-1.4.3.jar**
    * fire core jar which contains the fire Nodes and Workflow engine
* **data**
    * sample data files
* **src/main/java/fire/nodes/examples/NodeTestPrintFirstNRows.java**
    * A new node for Sparkflows
* **src/main/java/fire/workflows/examples/WorkflowTest.java**
    * example workflow which uses the node NodeTestPrintFirstNRows and executes it
* **pom.xml**
    * Maven pom.xml used to build this new node
* **README.md**
    * this README file which provides the steps of execution.

## Building

### Check out the code

Check out the code with : **git clone https://github.com/sparkflows/writing-new-node.git**

### Install the Fire jar to the local maven repository

Writing new Node depends on the Fire jar file. The Fire jar file provides the parent class for any new Node. Use the below commands to install the fire jar in your local maven repo.

    mvn install:install-file -Dfile=fire-spark_2_1-core-1.4.3.jar -DgroupId=fire  -DartifactId=fire-spark_2_1-core  -Dversion=1.4.3 -Dpackaging=jar
    
### Build with Maven

    mvn package

# Running the workflow on a Spark Cluster

Use the command below to load example data onto HDFS. It is then used by the example Workflow.

	hadoop fs -put data

Below is the command to execute the example Workflow on a Spark cluster. 

Executors with 1G and 1 vcore each have been specified in the commands. The parameter **'cluster'** specifies that we are running the workflow on a cluster as against locally. This greatly simplifies the development and debugging within the IDE by setting its value to **'local'** or not specifying it.

	spark-submit --class fire.workflows.examples.WorkflowTest --master yarn-client --executor-memory 1G  --num-executors 1  --executor-cores 1  target/writing-new-node-2.1.0-jar-with-dependencies.jar cluster


## Jar files

Building this repo generates the following jar files:

	target/writing-new-node-2.1.0.jar
	target/writing-new-node-2.1.0-jar-with-dependencies.jar

The details for coding a New Node is here : https://github.com/sparkflows/writing-new-node/blob/master/CreatingNewNodes.md


## Display the example Node in fire-ui and run it from there

New nodes written can be made visible in the Sparkflows UI. Thus, the users can start using them immediately.

* Copy the **writing-new-node-2.1.0.jar** to **fire-lib** directory of the sparkflows install
* Copy **testprintnrows.json** to the **nodes** directory under sparkflows install
* Restart fire-ui
* **TestPrintNRows** node would now be visible in the workflow editor window and you can start using it.


## Run a Java/Scala json workflow from the command line

The workflow can be created from the Sparkflows user interface. Each workflow has a json representation.

Below, the workflow is ExampleWorkflow.json

	spark-submit --class fire.execute.WorkflowExecuteFromFile --master yarn-client --executor-memory 1G  --num-executors 1  --executor-cores 1  target/writing-new-node-2.1.0-jar-with-dependencies.jar --workflow-file ExampleWorkflow.json

ExampleWorkflow.json consists of 3 nodes:

* fire.nodes.dataset.NodeDatasetTextFiles : Reads in the file data/cars.csv as a text file
* fire.nodes.etl.NodeFieldSplitter : Splits each line into columns c1,c2,c3,c4 using the comma as separator
* fire.nodes.examples.NodeTestPrintFirstNRows : Prints the first 10 records of the dataset





	


