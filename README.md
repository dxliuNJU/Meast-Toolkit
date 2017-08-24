# Meast-Toolkit is a toolkit for multi-entity-relation finding.
/MEAST contains the whole project of the toolkit.
/example contains an example entity-relation graph.</br>
/example/ExampleGraph.pdf is how the exapmle entity-relation graph look like.</br>
/example/ExampleTriples is the source data of the example.</br>
/meast.jar the runnable jar file. To run the example correctly, you have to juxtapose the example and meast.jar 
</br></br>
To running a completed progress, you may wish to run 'java -cp meast.jar name.dxliu.exapmle.Step1_ExampleTriplePreprocessor' for
the preprocessing and run 'java -cp meast.jar name.dxliu.exapmle.Step2_ExampleOracleUsage' to construct distance oracle.Then 'java -cp meast.jar name.dxliu.exapmle.Step1_ExampleTriplePreprocessor'
is how to query the relation between Alice,Bob and Chris in the example graph.
