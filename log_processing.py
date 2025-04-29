import sys

def main(file):

    try:
        with open(file) as f:
            lines = f.readlines()
        query_count = 0
        average_query_time = 0
        average_jdbc_time = 0

        for line in lines:
            line = line.strip("\n")
            timingSections = line.split(';')
            currTS = timingSections[0].split(":")
            currTS = currTS[1]
            currTJ = timingSections[1].split(":")
            currTJ = currTJ[1]
            
            average_query_time += int(currTS)
            average_jdbc_time += int(currTJ)
            query_count += 1

        print("Total number of queries: " + str(query_count))
        print("Average Search Servlet time: " + str(average_query_time / 1000000 / query_count) + "ms")
        print("Average JDBC time: " + str(average_jdbc_time / 1000000 / query_count) + "ms")
    except Exception as e:
        print("PARSING ERROR: " + e.__class__.__name__)

if __name__ == "__main__":
    main(sys.argv[1])