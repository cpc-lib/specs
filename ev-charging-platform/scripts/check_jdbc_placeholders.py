from pathlib import Path
import re, sys
root=Path(__file__).resolve().parents[1]
errors=[]; checked=0

def parse_string(s,i):
    if s.startswith('"""',i):
        j=s.find('"""',i+3)
        if j<0: raise ValueError('unterminated text block')
        return s[i+3:j],j+3
    if i<len(s) and s[i]=='"':
        out=[]; j=i+1; esc=False
        while j<len(s):
            c=s[j]
            if esc: out.append(c); esc=False
            elif c=='\\': esc=True; out.append(c)
            elif c=='"': return ''.join(out),j+1
            else: out.append(c)
            j+=1
        raise ValueError('unterminated string')
    return None,i

def matching_paren(s,start):
    depth=0; i=start; quote=None; textblock=False; esc=False
    while i<len(s):
        if textblock:
            if s.startswith('"""',i): textblock=False; i+=3; continue
            i+=1; continue
        c=s[i]
        if quote:
            if esc: esc=False
            elif c=='\\': esc=True
            elif c==quote: quote=None
            i+=1; continue
        if s.startswith('"""',i): textblock=True; i+=3; continue
        if c in ('"',"'"): quote=c; i+=1; continue
        if c=='(': depth+=1
        elif c==')':
            depth-=1
            if depth==0: return i
        i+=1
    return -1

def count_args(s):
    s=s.strip()
    if not s: return 0
    depth=0; count=1; quote=None; textblock=False; esc=False; i=0
    pairs={'(':')','[':']','{':'}'}
    while i<len(s):
        if textblock:
            if s.startswith('"""',i): textblock=False; i+=3; continue
            i+=1; continue
        c=s[i]
        if quote:
            if esc: esc=False
            elif c=='\\': esc=True
            elif c==quote: quote=None
            i+=1; continue
        if s.startswith('"""',i): textblock=True; i+=3; continue
        if c in ('"',"'"): quote=c; i+=1; continue
        if c in '([{': depth+=1
        elif c in ')]}': depth-=1
        elif c==',' and depth==0: count+=1
        i+=1
    return count

for p in root.rglob('*.java'):
    s=p.read_text(encoding='utf-8')
    for m in re.finditer(r'jdbc\.update\s*\(',s):
        open_idx=s.find('(',m.start())
        close=matching_paren(s,open_idx)
        if close<0: errors.append(f'{p.relative_to(root)}: unterminated jdbc.update'); continue
        i=open_idx+1
        while i<close and s[i].isspace(): i+=1
        sql,end=parse_string(s,i)
        if sql is None: continue
        j=end
        while j<close and s[j].isspace(): j+=1
        if j>=close: arg_count=0
        elif s[j]!=',': continue
        else: arg_count=count_args(s[j+1:close])
        placeholders=sql.count('?')
        checked+=1
        if placeholders!=arg_count:
            line=s.count('\n',0,m.start())+1
            errors.append(f'{p.relative_to(root)}:{line}: placeholders={placeholders} args={arg_count}')
print(f'JDBC_PLACEHOLDER_CHECK={"PASS" if not errors else "FAIL"} checked={checked}')
for e in errors: print('ERROR:',e)
sys.exit(1 if errors else 0)
