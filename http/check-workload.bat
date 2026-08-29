@echo off
REM One-time workload check for Giorgi.Janelidze (paste fresh token after re-login if expired)
set TOKEN=eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJkOGFkZWM3Yy0wZDljLTQ2MWMtYWMxNS0zOWRjODhlYTU2NzciLCJzdWIiOiJHaW9yZ2kuSmFuZWxpZHplIiwiaWF0IjoxNzg4MDE5MDk3LCJleHAiOjE3ODgwMjI2OTd9.vHM14NLgRpT7HdX3wkPCaDHSg_D01HZAoTwG5oV4JIA
echo Checking workload for Giorgi.Janelidze / 2026 / 8 ...
curl -s -H "Authorization: Bearer %TOKEN%" http://localhost:8082/workload/Giorgi.Janelidze/2026/8
echo.
pause
