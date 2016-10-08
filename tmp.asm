	section .data
s2 db "hoge"
s1 db "%s"
	section .text
	global _mymain
	extern _printf
_mymain:
	push rbp
	mov rbp, rsp
	mov rdi, s2
	mov rsi, s1
	call _printf
	leave
	ret
