# PMT (Predictive Mutation Testing)

Welcome to the home of Predictive Mutation Testing (PMT).


Mutation testing is a powerful methodology for evaluating
test suite quality. In mutation testing, a large number of
mutants are generated and executed against the test suite
to check the ratio of killed mutants. Therefore, mutation
testing is widely believed to be a computationally expensive
technique. To alleviate the efficiency concern of mutation
testing, we propose predictive mutation testing (PMT), 
the first approach to predicting mutation testing
results without mutant execution. In particular, the proposed
approach constructs a classification model based on
a series of features related to mutants and tests, and uses
the classification model to predict whether a mutant is killed
or survived without executing it. PMT has been evaluated
on 163 real-world projects under two application scenarios
(i.e., cross-version and cross-project). The experimental results
demonstrate that PMT improves the efficiency of mutation
testing by up to 151.4X while incurring only a small
accuracy loss when predicting mutant execution results, indicating
a good tradeoff between efficiency and effectiveness
of mutation testing.


arffs_for_weka： all the arffs used as training data and test data.
experi_results:  all the experimental results
project_info:  the project names, commit number (for the 9 base projects), and urls 
tools: our tools used for collecting test assertions and static metrics