@REM 用於更新application-local,yml的內容
@echo off
setlocal enabledelayedexpansion
set output_file=application-local.yml

if exist "%output_file%" del "%output_file%"

set currentDir=%CD%

for %%a in ("%currentDir%\.") do set dir1=%%~dpa
for %%b in ("%dir1%\.") do set dir2=%%~dpb
for %%c in ("%dir2%\.") do set dir3=%%~dpc
for %%d in ("%dir3%\.") do set dir4=%%~dpd
for %%e in ("%dir4%\.") do set dir5=%%~dpe

echo outputFileName: %dir5%BOT.
set cDir=%dir5%BOT

echo outputFileName: %cDir%

set rCurrentPath=%cDir:\=\\%

echo rCurrentPath: %rCurrentPath%
echo rootPath: "%rCurrentPath%" >> "%output_file%"
echo management: >> "%output_file%"
echo   endpoint: >> "%output_file%"
echo     shutdown: >> "%output_file%"
echo       enabled: true >> "%output_file%"
echo   endpoints: >> "%output_file%"
echo     web: >> "%output_file%"
echo       exposure: >> "%output_file%"
echo         include: health,shutdown,info >> "%output_file%"
echo. >> "%output_file%"

echo spring: >> "%output_file%"
echo   ds: >> "%output_file%"
echo     url: jdbc:sqlserver://192.168.6.5:1433;database=mis;encrypt=true;trustServerCertificate=true; >> "%output_file%"
echo     cn: com.microsoft.sqlserver.jdbc.SQLServerDriver >> "%output_file%"
echo     ur: misap >> "%output_file%"
echo     pw: 1qaz@WSX >> "%output_file%"
echo   jpa: >> "%output_file%"
echo     database-platform: org.hibernate.dialect.SQLServerDialect >> "%output_file%"
echo     generate-ddl: false >> "%output_file%"
echo     properties: >> "%output_file%"
echo       hibernate: >> "%output_file%"
echo         jdbc: >> "%output_file%"
echo           fetch_size: 50 >> "%output_file%"
echo           batch_size: 30 >> "%output_file%"
echo         show_sql: true >> "%output_file%"
echo         format_sql: true >> "%output_file%" >> "%output_file%"
echo. >> "%output_file%"

echo   log: >> "%output_file%"
echo     path: "..\\LOG\\MIS\\" >> "%output_file%"
echo   application: >> "%output_file%"
echo     name: mis >> "%output_file%"
echo     rec: true >> "%output_file%"
echo     pt: false >> "%output_file%"
echo     fsapSyncFg: false >> "%output_file%"
echo. >> "%output_file%"

echo   fsapSyncSftp: >> "%output_file%"
echo     host: 220.135.109.130 >> "%output_file%"
echo     port: 55688 >> "%output_file%"
echo     usr: weblogic >> "%output_file%"
echo     pw: 1qaz2wsx >> "%output_file%"
echo     poolsize: 20 >> "%output_file%"
echo     timeout: 20000 >> "%output_file%"
echo     localroot: ../temp/ >> "%output_file%"
echo     remoteroot: /home/weblogic/uploadTemp/ >> "%output_file%"
echo. >> "%output_file%"

echo server: >> "%output_file%"
echo   port: 80 >> "%output_file%"
echo   address: 0.0.0.0 >> "%output_file%"
echo   compression: >> "%output_file%"
echo     enabled: true >> "%output_file%" >> "%output_file%"
echo. >> "%output_file%"

echo grpc: >> "%output_file%"
echo   server: >> "%output_file%"
echo     port: 8081 >> "%output_file%"
echo     address: "0.0.0.0" >> "%output_file%"
echo     corePoolSize: 50 >> "%output_file%"
echo     maxPoolSize: 200 >> "%output_file%"
echo     threadQueueSize: 200 >> "%output_file%" >> "%output_file%"
echo. >> "%output_file%"

echo astar: >> "%output_file%"
echo   binPath: "astar/" >> "%output_file%" >> "%output_file%"
echo. >> "%output_file%"
echo apiNodes: ^> >> "%output_file%"

set count=0
for %%f in (apiNode_*.yml) do (
    set /a count+=1
)

echo %count% 
set current=0

for %%f in (apiNode_*.yml) do (
    set /a current+=1
    
    echo !current!.fileName="%%f
    if  !current! == !count! (
        echo   %%f>> "%output_file%"
    ) else (
       
        echo   %%f,>> "%output_file%"
    )

)
echo. >> "%output_file%"
echo xmlPath: "src\\main\\resources\\xml" >> "%output_file%"
echo. >> "%output_file%"
echo localFile: >> "%output_file%"
echo   mis: >> "%output_file%"
echo     batch: >> "%output_file%"
echo       bots_input: "..\\BOT\\MIS\\BH\\BOTS_INPUT\\" >> "%output_file%"
echo       bots_output: "..\\BOT\\MIS\\BH\\BOTS_OUTPUT\\" >> "%output_file%"
echo       input: "..\\BOT\\MIS\\BH\\INPUT\\" >> "%output_file%"
echo       output: "..\\BOT\\MIS\\BH\\OUTPUT\\" >> "%output_file%"
echo     xml: >> "%output_file%"
echo       input: >> "%output_file%"
echo         directory: "${xmlPath}\\input\\" >> "%output_file%"
echo       output: >> "%output_file%"
echo         directory: "${xmlPath}\\output\\" >> "%output_file%"
echo       mask: >> "%output_file%"
echo         directory: "${xmlPath}\\mask\\" >> "%output_file%" >> "%output_file%"
echo         convert: "${xmlPath}\\mask\\convert\\" >> "%output_file%" >> "%output_file%"
echo       todb: >> "%output_file%"
echo         directory: "${xmlPath}\\todb\\" >> "%output_file%" >> "%output_file%"
echo. >> "%output_file%"

echo externalSort: >> "%output_file%"
echo   blockSize: 100000 >> "%output_file%"

echo Finished generating application.yaml file.
endlocal


@echo off
setlocal enabledelayedexpansion



echo Finished writing file names with commas and asterisk to %output_file%.

pause 