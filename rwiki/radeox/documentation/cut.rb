def cut(fileName, start, stop)
    inHandle = File::open( fileName )

    inText = inHandle.read()
    result = "ERROR #{fileName}"
    inText.scan(/\/\/.*?cut:#{start}.*?\n(.*?)\n\/\/[^\/]*cut:#{stop}/m) do |block|
      lines = block[0].split("\n")
      gobble = countSpace( lines )

      beginBlock = "\\begin{Verbatim}[gobble=#{gobble},frame=single,numbers=left,fontsize=\\small]\n"
      endBlock = "\\end{Verbatim}"
      result = block[0]
      result = "%!SRC|#{fileName}|#{start}|#{stop}|\n" + "#{beginBlock}"  + result + "\n#{endBlock}\n%!END"
    end
    result
end

def countSpace( lines )
  count = 1000
  lines.each do |line|
       if line.length>0
       currentCount = 0
       while line[currentCount,1] == " "
          currentCount = currentCount + 1
       end
       if count > currentCount 
          count = currentCount
       end
     end
  end
  count
end

fileName = ARGV[0]
fHandle = File::open( fileName )
inText = fHandle.read()
found = false

inText.gsub!(/\%\!SRC\|(.*?)\|(.*?)\|(.*?)\|.*?\n(.*?)%!END/m) do |match|
    gobble=4
    beginBlock = "\\begin{Verbatim}[gobble=#{gobble},frame=single,numbers=left]\n"
    endBlock = "\\end{Verbatim}"
    result = match.split("|") 
    cut(result[1], result[2], result[3])
end
print inText
