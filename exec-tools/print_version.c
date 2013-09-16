#include <stdio.h>
#include "ossVer.h"
int main()
{
   printf("%d.%d",SDB_ENGINE_VERISON_CURRENT , SDB_ENGINE_SUBVERSION_CURRENT);
   return 0;
}