#!/usr/bin/env ruby

require 'fileutils'
require 'optparse'

BASENAME = File.basename(__FILE__)
VERSION = '0.1.0'

opts = {
  comp_dst: './css',
  conf_src: './sass/theme',
  sass_src: './sass'
}

parser = OptionParser.new do |o|
  o.banner = "Usage: #{BASENAME} [OPTION]..."
  o.separator ''
  o.separator 'Spectific options:'

  o.on '-c', '--compile-destination DIR', 'Compile destination' do |val|
    opts[:comp_dst] = val
  end

  o.on('-C', '--config-directory DIR',
       'Theme configuration source directory') do |val|
    opts[:conf_src] = val
  end

  o.on('-s', '--sass-directory DIR', 'SASS source directory') do |val|
    opts[:sass_src] = val
  end

  o.separator ''
  o.separator 'General options:'

  o.on '-h', '--help', 'Display this help message.' do
    puts o.help
    exit 0
  end

  o.on '--version', 'Display the version number' do
    puts "#{VERSION}\n"
    exit 0
  end

  o.separator ''
  o.separator "#{BASENAME} home page: <https://gist.github.com/zellio/0c666adb961a72618e44>"
  o.separator 'Report bugs to: <zach@nyu.edu>'
  o.separator ''

end

parser.parse!(ARGV)

unless ARGV.length.equal? 0
  puts parser.help
  exit(1);
end

Dir.glob(File.expand_path('*.scss', opts[:conf_src])).each do |file|
  next unless File.file?(file)

  # Copy configuration from the theme directory
  FileUtils.cp(file, "#{opts[:sass_src]}/_configurations.scss")

  # Extract theme name from config file
  build_name = /_(.+)\.scss$/.match(File.basename(file))[1]

  cmd = "compass compile " +
    "--sass-dir #{opts[:sass_src]} " +
    "--css-dir ../#{build_name}/"

  puts cmd
  system(cmd)
  puts ""
end

# Erase the configuration file
File.open("#{opts[:sass_src]}/_configurations.scss", "w")
