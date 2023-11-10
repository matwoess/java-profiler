$cocoJarUrl = "https://ssw.jku.at/Research/Projects/Coco/Java/Coco.jar"
$projectDir = (Get-Item -Path $MyInvocation.MyCommand.Path).Directory.Parent.FullName
Set-Location $projectDir
New-Item -ItemType Directory -Force -Path ".\lib"
if (-Not (Test-Path ".\lib\Coco.jar")) {
    Invoke-WebRequest -Uri $cocoJarUrl -OutFile ".\lib\Coco.jar"
}
java -jar lib/Coco.jar -package tool.instrument profiler-tool/src/main/java/tool/instrument/JavaFile.atg
