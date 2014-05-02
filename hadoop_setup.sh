hadoop fs -mkdir /user/davidmedinets
hadoop fs -chown davidmedinets /user/davidmedinets

hadoop fs -mkdir /user/davidmedinets/failures
hadoop fs -chown davidmedinets /user/davidmedinets/failures

alias hls="hadoop fs -ls "
alias hlsr="hadoop fs -lsr "
alias hcat="hadoop fs -cat "

